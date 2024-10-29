package `in`.dragonbra.javasteam.types

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ContentManifest.ContentManifestSignature
import `in`.dragonbra.javasteam.base.ContentManifest.ContentManifestMetadata
import `in`.dragonbra.javasteam.base.ContentManifest.ContentManifestPayload
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.InputStream
import java.io.OutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.util.Date
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.NoSuchElementException


/**
 * Represents a Steam3 depot manifest.
 */
class DepotManifest {
    companion object {
        const val PROTOBUF_PAYLOAD_MAGIC = 0x71F617D0
        const val PROTOBUF_METADATA_MAGIC = 0x1F4812BE
        const val PROTOBUF_SIGNATURE_MAGIC = 0x1B81B817
        const val PROTOBUF_ENDOFMANIFEST_MAGIC = 0x32C415AB

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param stream Raw depot manifest stream to deserialize.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        fun deserialize(stream: InputStream): Pair<DepotManifest, ByteArray> {
            val manifest = DepotManifest()
            val checksum = CryptoHelper.shaHash(stream.readBytes())
            manifest.internalDeserialize(stream)
            return Pair(manifest, checksum)
        }

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param data Raw depot manifest data to deserialize.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        fun deserialize(data: ByteArray): Pair<DepotManifest, ByteArray> {
            return MemoryStream(data).use { ms -> deserialize(ms) }
        }

        /**
         * Loads binary manifest from a file and deserializes it.
         * @param filename Input file name.
         * @return [Pair]<[DepotManifest], [ByteArray]> object where the first value is the depot manifest
         * and the second is the checksum if the deserialization was successful or else the values will be null.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        fun loadFromFile(filename: String): Pair<DepotManifest, ByteArray>? {
            val file = File(filename)
            if (!file.exists())
                return null

            return FileInputStream(file).use { fs ->
                deserialize(fs)
            }
        }
    }

    /**
     * Gets the list of files within this manifest.
     */
    val files: MutableList<FileData>
    /**
     * Gets a value indicating whether filenames within this depot are encrypted.
     */
    var filenamesEncrypted: Boolean = false
        private set

    /**
     * Gets the depot id.
     */
    var depotID: Int = 0
        private set

    /**
     * Gets the manifest id.
     */
    var manifestGID: Long = 0
        private set

    /**
     * Gets the depot creation time.
     */
    var creationTime: Date = Date()
        private set

    /**
     * Gets the total uncompressed size of all files in this depot.
     */
    var totalUncompressedSize: Long = 0
        private set

    /**
     * Gets the total compressed size of all files in this depot.
     */
    var totalCompressedSize: Long = 0
        private set

    /**
     * Gets CRC-32 checksum of encrypted manifest payload.
     */
    var encryptedCRC: Int = 0
        private set

    constructor() {
        files = mutableListOf()
        filenamesEncrypted = false
        depotID = 0
        manifestGID = 0
        creationTime = Date()
        totalUncompressedSize = 0
        totalCompressedSize = 0
        encryptedCRC = 0
    }
    constructor(manifest: DepotManifest) {
        files = manifest.files.map { FileData(it) }.toMutableList()
        filenamesEncrypted = manifest.filenamesEncrypted
        depotID = manifest.depotID
        manifestGID = manifest.manifestGID
        creationTime = manifest.creationTime
        totalUncompressedSize = manifest.totalUncompressedSize
        totalCompressedSize = manifest.totalCompressedSize
        encryptedCRC = manifest.encryptedCRC
    }

    /**
     * Attempts to decrypt file names with the given encryption key.
     * @param encryptionKey The encryption key.
     * @return `true` if the file names were successfully decrypted; otherwise `false`.
     */
    fun decryptFilenames(encryptionKey: ByteArray): Boolean {
        if (!filenamesEncrypted) {
            return true
        }

        assert(files != null) { "Files was null when attempting to decrypt filenames." }
        assert(encryptionKey.size == 32) { "Decrypt filenames used with non 32 byte key!" }

        // This was originally copy-pasted in the SteamKit2 source from CryptoHelper.SymmetricDecrypt to avoid allocating Aes instance for every filename
        // and now ported to Kotlin with the help of Claude 3.5 Sonnet
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val secretKey = SecretKeySpec(encryptionKey, "AES")
        val iv = ByteArray(16)
        var filenameLength = 0
        var bufferDecoded = ByteArray(256)
        var bufferDecrypted = ByteArray(256)

        try {
            for (file in files) {
                val decodedLength = file.fileName.length / 4 * 3 // This may be higher due to padding

                // Majority of filenames are short, even when they are encrypted and base64 encoded,
                // so this resize will be hit *very* rarely
                if (decodedLength > bufferDecoded.size) {
                    bufferDecoded = ByteArray(decodedLength)
                    bufferDecrypted = ByteArray(decodedLength)
                }

                val decoded = Base64.getDecoder().decode(file.fileName)
                if (decoded == null) {
                    assert(false) { "Failed to base64 decode the filename." }
                    return false
                }
                System.arraycopy(decoded, 0, bufferDecoded, 0, decoded.size)

                try {
                    val encryptedFilename = bufferDecoded.sliceArray(decoded.indices)

                    // Extract IV from the first 16 bytes
                    System.arraycopy(encryptedFilename, 0, iv, 0, 16)

                    // Decrypt filename
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
                    filenameLength = cipher.doFinal(encryptedFilename, 16, encryptedFilename.size - 16, bufferDecrypted)
                } catch (e: Exception) {
                    assert(false) { "Failed to decrypt the filename." }
                    return false
                }

                // Trim the ending null byte, safe for UTF-8
                if (filenameLength > 0 && bufferDecrypted[filenameLength] == 0.toByte()) {
                    filenameLength--
                }

                // ASCII is subset of UTF-8, so it safe to replace the raw bytes here
//                bufferDecrypted.forEachIndexed { index, byte ->
//                    if (byte == altDirChar.toByte()) {
//                        bufferDecrypted[index] = File.separatorChar.toByte()
//                    }
//                }

                file.fileName = String(bufferDecrypted, 0, filenameLength, Charsets.UTF_8)
            }
        } finally {
            // In Kotlin, we don't need to explicitly return buffers to a pool
        }

        // Sort file entries alphabetically because that's what Steam does
        // TODO: Doesn't match Steam sorting if there are non-ASCII names present
        files.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.fileName })

        filenamesEncrypted = false
        return true
    }

    /**
     * Serializes depot manifest and saves the output to a file.
     * @param filename Output file name.
     */
    fun saveToFile(filename: String): ByteArray {
        FileOutputStream(File(filename)).use { fs ->
            return serialize(fs)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal fun internalDeserialize(stream: InputStream) {
        var payload: ContentManifestPayload? = null
        var metadata: ContentManifestMetadata? = null
        var signature: ContentManifestSignature? = null

        BinaryReader(stream).use { br ->
            while (true) {
                val magic = br.readInt()

                if (magic == PROTOBUF_ENDOFMANIFEST_MAGIC) {
                    break
                }

                when(magic) {
                    Steam3Manifest.MAGIC -> {
                        val binaryManifest = Steam3Manifest.deserialize(br)
                        parseBinaryManifest(binaryManifest)

                        val marker = br.readInt()
                        if (marker != magic)
                            throw NoSuchElementException("Unable to find end of message marker for depot manifest")
                    }
                    PROTOBUF_PAYLOAD_MAGIC -> {
                        val payloadLength = br.readInt()
                        payload = ContentManifestPayload.parseFrom(stream.readNBytes(payloadLength))
                    }
                    PROTOBUF_METADATA_MAGIC -> {
                        val metadataLength = br.readInt()
                        metadata = ContentManifestMetadata.parseFrom(stream.readNBytes(metadataLength))
                    }
                    PROTOBUF_SIGNATURE_MAGIC -> {
                        val signatureLength = br.readInt()
                        signature = ContentManifestSignature.parseFrom(stream.readNBytes(signatureLength))
                    }
                    else -> throw NoSuchElementException("Unrecognized magic value ${magic.toHexString(HexFormat.Default)} in depot manifest.")
                }
            }
        }

        if (payload != null && metadata != null && signature != null) {
            parseProtobufManifestMetadata(metadata!!)
            parseProtobufManifestPayload(payload!!)
        } else {
            throw NoSuchElementException("Missing ContentManifest sections required for parsing depot manifest")
        }
    }
    internal fun parseBinaryManifest(manifest: Steam3Manifest) {
        files.clear()
        filenamesEncrypted = manifest.areFileNamesEncrypted
        depotID = manifest.depotID
        manifestGID = manifest.manifestGID
        creationTime = manifest.creationTime
        totalUncompressedSize = manifest.totalUncompressedSize
        totalCompressedSize = manifest.totalCompressedSize

        for (fileMapping in manifest.fileMapping) {
            val fileData = FileData(
                fileName = fileMapping.fileName,
                fileNameHash = fileMapping.hashFileName,
                flags = fileMapping.flags,
                totalSize = fileMapping.totalSize,
                fileHash = fileMapping.hashContent,
                linkTarget = ""
            )

            for (chunk in fileMapping.chunks) {
                fileData.chunks.add(ChunkData(
                    chunkID = chunk.chunkGID,
                    checksum = chunk.checksum,
                    offset = chunk.offset,
                    compressedLength = chunk.compressedSize,
                    uncompressedLength = chunk.decompressedSize
                ))
            }
            files.add(fileData)
        }
    }
    internal fun parseProtobufManifestPayload(payload: ContentManifestPayload) {
        files.clear()

        for (fileMapping in payload.mappingsList) {
            val fileData = FileData(
                fileName = fileMapping.filename,
                fileNameHash = fileMapping.shaFilename.toByteArray(),
                flags = EDepotFileFlag.from(fileMapping.flags),
                totalSize = fileMapping.size,
                fileHash = fileMapping.shaContent.toByteArray(),
                linkTarget = fileMapping.linktarget
            )

            for (chunk in fileMapping.chunksList) {
                fileData.chunks.add(ChunkData(
                    chunkID = chunk.sha.toByteArray(),
                    checksum = chunk.crc,
                    offset = chunk.offset,
                    compressedLength = chunk.cbCompressed,
                    uncompressedLength = chunk.cbOriginal))
            }
            files.add(fileData)
        }
    }
    internal fun parseProtobufManifestMetadata(metadata: ContentManifestMetadata) {
        filenamesEncrypted = metadata.filenamesEncrypted
        depotID = metadata.depotId
        manifestGID = metadata.gidManifest
        creationTime = Date.from(Instant.ofEpochSecond(metadata.creationTime.toLong()))
        totalUncompressedSize = metadata.cbDiskOriginal
        totalCompressedSize = metadata.cbDiskCompressed
        encryptedCRC = metadata.crcEncrypted
    }

    /**
     * Serializes the depot manifest into the provided output stream.
     * @param output The stream to which the serialized depot manifest will be written.
     */
    fun serialize(output: OutputStream): ByteArray {
        assert(files != null) { "Files was null when attempting to serialize manifest." }

        val payload = ContentManifestPayload.newBuilder()
        val uniqueChunks = hashSetOf<ByteArray>()

        for (file in files) {
            val protoFile = ContentManifestPayload.FileMapping.newBuilder()
            protoFile.setSize(file.totalSize)
            protoFile.setFlags(EDepotFileFlag.code(file.flags))
            if (filenamesEncrypted) {
                // Assume the name is unmodified
                protoFile.setFilename(file.fileName)
                protoFile.setShaFilename(ByteString.copyFrom(file.fileNameHash))
            } else {
                protoFile.setFilename(file.fileName.replace('/', '\\'))
                protoFile.setShaFilename(
                    ByteString.copyFrom(
                        CryptoHelper.shaHash(file.fileName
                            .replace('/', '\\')
                            .lowercase()
                            .toByteArray(Charsets.UTF_8)
                        )
                    )
                )
            }
            protoFile.setShaContent(ByteString.copyFrom(file.fileHash))
            if (file.linkTarget.isNotBlank()) {
                protoFile.linktarget = file.linkTarget
            }

            for (chunk in file.chunks) {
                val protoChunk = ContentManifestPayload.FileMapping.ChunkData.newBuilder()
                protoChunk.setSha(ByteString.copyFrom(chunk.chunkID))
                protoChunk.setCrc(chunk.checksum)
                protoChunk.setOffset(chunk.offset)
                protoChunk.setCbOriginal(chunk.uncompressedLength)
                protoChunk.setCbCompressed(chunk.compressedLength)

                protoFile.addChunks(protoChunk)
                uniqueChunks.add(chunk.chunkID!!)
            }

            payload.addMappings(protoFile)
        }

        val metadata = ContentManifestMetadata.newBuilder()
        metadata.setDepotId(depotID)
        metadata.setGidManifest(manifestGID)
        metadata.setCreationTime((creationTime.toInstant().epochSecond).toInt())
        metadata.setFilenamesEncrypted(filenamesEncrypted)
        metadata.setCbDiskOriginal(totalUncompressedSize)
        metadata.setCbDiskCompressed(totalCompressedSize)
        metadata.setUniqueChunks(uniqueChunks.size)

        // Calculate payload CRC
        val payloadData = payload.build().toByteArray()
        val len = payloadData.size
        val data = ByteArray(Int.SIZE_BYTES + len)
        System.arraycopy(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(len), 0, data, 0, 4)
        System.arraycopy(payloadData, 0, data, 4, len)
        val crc32 = Utils.crc32(payloadData).toInt()

        if (filenamesEncrypted) {
            metadata.setCrcEncrypted(crc32)
            metadata.setCrcClear(0)
        } else {
            metadata.setCrcEncrypted(encryptedCRC)
            metadata.setCrcClear(crc32)
        }

        // Write the manifest to the stream and return the checksum
        val manifestBytes: ByteArray
        ByteArrayOutputStream().use { bw ->
            // Write Protobuf payload
            bw.write(PROTOBUF_PAYLOAD_MAGIC)
            bw.write(payloadData.size)
            bw.write(payloadData)

            // Write Protobuf metadata
            val metadataData = metadata.build().toByteArray()
            bw.write(PROTOBUF_METADATA_MAGIC)
            bw.write(metadataData.size)
            bw.write(metadataData)

            // Write empty signature section
            bw.write(PROTOBUF_SIGNATURE_MAGIC)
            bw.write(0)

            // Write EOF marker
            bw.write(PROTOBUF_ENDOFMANIFEST_MAGIC)

            manifestBytes = bw.toByteArray()
        }
        DataOutputStream(output).use { bw ->
            bw.write(manifestBytes)
        }
        return CryptoHelper.shaHash(manifestBytes)
    }

    fun calculateChecksum(): ByteArray {
        return ByteArrayOutputStream().use { bs ->
            serialize(bs)
        }
    }
}
