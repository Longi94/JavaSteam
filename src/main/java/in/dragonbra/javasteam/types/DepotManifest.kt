package `in`.dragonbra.javasteam.types

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestPayload
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestMetadata
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestSignature
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.InputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant
import java.util.Date
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Represents a Steam3 depot manifest.
 */
@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
class DepotManifest {

    companion object {
        const val PROTOBUF_PAYLOAD_MAGIC = 0x71F617D0
        const val PROTOBUF_METADATA_MAGIC = 0x1F4812BE
        const val PROTOBUF_SIGNATURE_MAGIC = 0x1B81B817
        const val PROTOBUF_ENDOFMANIFEST_MAGIC = 0x32C415AB
        private val logger: Logger = LogManager.getLogger(DepotManifest::class.java)

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param stream Raw depot manifest stream to deserialize.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        fun deserialize(stream: InputStream): Pair<DepotManifest, ByteArray> {
            return deserialize(stream.readBytes())
        }

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param data Raw depot manifest data to deserialize.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        fun deserialize(data: ByteArray): Pair<DepotManifest, ByteArray> {
            val checksum = CryptoHelper.shaHash(data)
            return MemoryStream(data).use { ms ->
                val manifest = DepotManifest()
                manifest.internalDeserialize(ms)
                Pair(manifest, checksum)
            }
        }

        /**
         * Loads binary manifest from a file and deserializes it.
         * @param filename Input file name.
         * @return [Pair]<[DepotManifest], [ByteArray]> object where the first value is the depot manifest
         * and the second is the checksum if the deserialization was successful or else the values will be null.
         * @exception NoSuchElementException Thrown if the given data is not something recognizable.
         */
        @Suppress("unused")
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

        assert(encryptionKey.size == 32) { "Decrypt filenames used with non 32 byte key!" }

        // This was originally copy-pasted in the SteamKit2 source from CryptoHelper.SymmetricDecrypt to avoid allocating Aes instance for every filename
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        val aes = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        val secretKey = SecretKeySpec(encryptionKey, "AES")
        var iv: ByteArray

        try {
            for (file in files) {
                val decoded = Base64.getUrlDecoder().decode(file.fileName
                    .replace('+', '-')
                    .replace('/', '_')
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "")
                )

                val bufferDecrypted: ByteArray
                try {
                    // Extract IV from the first 16 bytes
                    ecbCipher.init(Cipher.DECRYPT_MODE, secretKey)
                    iv = ecbCipher.doFinal(decoded, 0, 16)

                    // Decrypt filename
                    aes.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
                    bufferDecrypted = aes.doFinal(decoded, iv.size, decoded.size - iv.size)
                } catch (e: Exception) {
                    logger.error("Failed to decrypt the filename: $e")
                    return false
                }

                // Trim the ending null byte, safe for UTF-8
                val filenameLength = bufferDecrypted.size - if (bufferDecrypted.isNotEmpty() && bufferDecrypted[bufferDecrypted.size - 1] == 0.toByte()) 1 else 0

                file.fileName = String(bufferDecrypted, 0, filenameLength, Charsets.UTF_8).replace('\\', File.separatorChar)
            }
        } catch (e: Exception) {
            logger.error("Failed to decrypt filenames: $e")
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
    @Suppress("unused")
    fun saveToFile(filename: String): ByteArray {
        FileOutputStream(File(filename)).use { fs ->
            return serialize(fs).second
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    internal fun internalDeserialize(stream: MemoryStream) {
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
     * @return A pair object containing the amount of bytes written and the checksum of the manifest
     */
    fun serialize(output: OutputStream): Pair<Int, ByteArray> {
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
        System.arraycopy(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(len).array(), 0, data, 0, 4)
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
        val manifestBytes = ByteArrayOutputStream().use { bw ->
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

            bw.toByteArray()
        }
        output.write(manifestBytes)
        return Pair(manifestBytes.size, CryptoHelper.shaHash(manifestBytes))
    }

    fun calculateChecksum(): ByteArray {
        return ByteArrayOutputStream().use { bs ->
            serialize(bs).second
        }
    }
}
