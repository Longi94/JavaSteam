package `in`.dragonbra.javasteam.types

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestMetadata
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestPayload
import `in`.dragonbra.javasteam.protobufs.steamclient.ContentManifest.ContentManifestSignature
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.BinaryWriter
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

/**
 * Represents a Steam3 depot manifest.
 */
@Suppress("unused")
class DepotManifest {

    companion object {

        private val logger: Logger = LogManager.getLogger<DepotManifest>()

        private const val PROTOBUF_PAYLOAD_MAGIC: Int = 0x71F617D0
        private const val PROTOBUF_METADATA_MAGIC: Int = 0x1F4812BE
        private const val PROTOBUF_SIGNATURE_MAGIC: Int = 0x1B81B817
        private const val PROTOBUF_ENDOFMANIFEST_MAGIC: Int = 0x32C415AB

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param stream  Raw depot manifest stream to deserialize.
         */
        @JvmStatic
        fun deserialize(stream: InputStream): DepotManifest {
            val manifest = DepotManifest()
            manifest.internalDeserialize(stream)
            return manifest
        }

        /**
         * Initializes a new instance of the [DepotManifest] class.
         * Depot manifests may come from the Steam CDN or from Steam/depotcache/ manifest files.
         * @param data Raw depot manifest data to deserialize.
         */
        @JvmStatic
        fun deserialize(data: ByteArray): DepotManifest {
            val ms = MemoryStream(data)
            val manifest = deserialize(ms)
            ms.close()
            return manifest
        }

        /**
         * Loads binary manifest from a file and deserializes it.
         * @param filename Input file name.
         * @return [DepotManifest] object if deserialization was successful; otherwise, **null**.
         */
        @JvmStatic
        fun loadFromFile(filename: String): DepotManifest? {
            val file = File(filename)
            if (!file.exists()) {
                return null
            }

            return file.inputStream().use { fileStream ->
                deserialize(fileStream)
            }
        }
    }

    /**
     * Gets the list of files within this manifest.
     */
    var files: ArrayList<FileData> = arrayListOf()

    /**
     * Gets a value indicating whether filenames within this depot are encrypted.
     * @return **true** if the filenames are encrypted; otherwise, <c>false</c>.
     */
    var filenamesEncrypted: Boolean = false

    /**
     * Gets the depot id.
     */
    var depotID: Int = 0

    /**
     * Gets the manifest id.
     */
    var manifestGID: Long = 0L

    /**
     * Gets the depot creation time.
     */
    var creationTime: Date = Date()

    /**
     * Gets the total uncompressed size of all files in this depot.
     */
    var totalUncompressedSize: Long = 0L

    /**
     * Gets the total compressed size of all files in this depot.
     */
    var totalCompressedSize: Long = 0L

    /**
     * Gets CRC-32 checksum of encrypted manifest payload.
     */
    var encryptedCRC: Int = 0

    /**
     * Attempts to decrypt file names with the given encryption key.
     * @param encryptionKey The encryption key.
     * @return **true** if the file names were successfully decrypted; otherwise, **false**.
     */
    fun decryptFilenames(encryptionKey: ByteArray): Boolean {
        if (!filenamesEncrypted) {
            return true
        }

        require(encryptionKey.size == 32) { "Decrypt filnames used with non 32 byte key!" }

        // This was originally copy-pasted in the SteamKit2 source from CryptoHelper.SymmetricDecrypt to avoid allocating Aes instance for every filename
        val ecbCipher = Cipher.getInstance("AES/ECB/NoPadding", CryptoHelper.SEC_PROV)
        val aes = Cipher.getInstance("AES/CBC/PKCS7Padding", CryptoHelper.SEC_PROV)
        val secretKey = SecretKeySpec(encryptionKey, "AES")

        val iv = ByteArray(16)
        var filenameLength: Int
        var bufferDecoded = ByteArray(256)
        var bufferDecrypted = ByteArray(256)

        // SK has this below `return true;` but we cannot do that in Kotlin.
        fun tryDecryptName(name: String, iv: ByteArray, decoded: (String?) -> Unit): Boolean {
            decoded(null)

            var decodedLength = name.length / 4 * 3 // This may be higher due to padding

            // Majority of filenames are short, even when they are encrypted and base64 encoded,
            // so this resize will be hit *very* rarely
            if (decodedLength > bufferDecoded.size) {
                bufferDecoded = ByteArray(decodedLength)
                bufferDecrypted = ByteArray(decodedLength)
            }

            val decoder = Base64.getUrlDecoder()
            decodedLength = try {
                val tempBytes = decoder.decode(
                    name
                        .replace('+', '-')
                        .replace('/', '_')
                        .replace("\n", "")
                        .replace("\r", "")
                        .replace(" ", "")
                )
                if (tempBytes.size <= bufferDecoded.size) {
                    tempBytes.copyInto(bufferDecoded)
                    tempBytes.size
                } else {
                    // Buffer too small
                    throw IllegalArgumentException("Buffer too small")
                }
            } catch (e: Exception) {
                logger.error("Failed to base64 decode the filename: ${e.message}", e)
                return false
            }

            try {
                // Get a slice of the decoded buffer up to decodedLength
                val encryptedFilename = bufferDecoded.copyOfRange(0, decodedLength)

                // Decrypt the IV portion (first 16 bytes) using ECB mode
                ecbCipher.init(Cipher.DECRYPT_MODE, secretKey)
                ecbCipher.doFinal(encryptedFilename, 0, iv.size, iv, 0)

                // Decrypt the rest using CBC mode with the IV we just decrypted
                val ivSpec = IvParameterSpec(iv)
                aes.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

                // Decrypt the remaining data after the IV
                val remainingData = encryptedFilename.copyOfRange(iv.size, encryptedFilename.size)
                filenameLength = aes.doFinal(remainingData, 0, remainingData.size, bufferDecrypted, 0)
            } catch (e: Exception) {
                logger.error("Failed to decrypt the filename.", e)
                return false
            }

            // Trim the ending null byte, safe for UTF-8
            if (filenameLength > 0 && bufferDecrypted[filenameLength - 1] == 0.toByte()) {
                filenameLength--
            }

            for (i in 0 until filenameLength) {
                if (bufferDecrypted[i] == '\\'.code.toByte()) {
                    bufferDecrypted[i] = File.separatorChar.code.toByte()
                }
            }

            decoded(String(bufferDecrypted, 0, filenameLength, Charsets.UTF_8))
            return true
        }

        try {
            files.forEach { file ->
                var name: String? = null

                if (!tryDecryptName(name = file.fileName, iv = iv, decoded = { name = it })) {
                    return false
                }

                file.fileName = name.orEmpty()

                var linkName: String? = null
                if (!file.linkTarget.isNullOrEmpty()) {
                    if (!tryDecryptName(name = file.linkTarget!!, iv = iv, decoded = { linkName = it })) {
                        return false
                    }

                    file.linkTarget = linkName
                }
            }
        } finally {
            bufferDecoded.fill(0)
            bufferDecrypted.fill(0)
        }

        // Sort file entries alphabetically because that's what Steam does
        // TODO: (SK) Doesn't match Steam sorting if there are non-ASCII names present
        files.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.fileName })

        filenamesEncrypted = false
        return true
    }

    /**
     * Serializes depot manifest and saves the output to a file.
     * @param filename Output file name.
     */
    fun saveToFile(filename: String) {
        File(filename).outputStream().use { fileStream ->
            serialize(fileStream)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun internalDeserialize(stream: InputStream) {
        var payload: ContentManifestPayload? = null
        var metadata: ContentManifestMetadata? = null
        var signature: ContentManifestSignature? = null

        BinaryReader(stream).use { br ->
            while (true) {
                val magic = br.readInt()

                if (magic == PROTOBUF_ENDOFMANIFEST_MAGIC) {
                    break
                }

                when (magic) {
                    Steam3Manifest.MAGIC -> {
                        val binaryManifest = Steam3Manifest()
                        binaryManifest.deserialize(br)
                        parseBinaryManifest(binaryManifest)

                        val marker = br.readInt()
                        if (marker != magic) {
                            throw NoSuchElementException("Unable to find end of message marker for depot manifest")
                        }

                        // This is an intentional return because v4 manifest does not have the separate sections,
                        // and it will be parsed by ParseBinaryManifest. If we get here, the entire buffer has been already processed.
                        return
                    }

                    PROTOBUF_PAYLOAD_MAGIC -> {
                        val payloadLength = br.readInt()
                        payload = ContentManifestPayload.parseFrom(stream.readNBytesCompat(payloadLength))
                    }

                    PROTOBUF_METADATA_MAGIC -> {
                        val metadataLength = br.readInt()
                        metadata = ContentManifestMetadata.parseFrom(stream.readNBytesCompat(metadataLength))
                    }

                    PROTOBUF_SIGNATURE_MAGIC -> {
                        val signatureLength = br.readInt()
                        signature = ContentManifestSignature.parseFrom(stream.readNBytesCompat(signatureLength))
                    }

                    else -> {
                        throw NoSuchElementException("Unrecognized magic value ${magic.toHexString()} in depot manifest.")
                    }
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

    private fun parseBinaryManifest(manifest: Steam3Manifest) {
        files = ArrayList(manifest.mapping.size)
        filenamesEncrypted = manifest.areFileNamesEncrypted
        depotID = manifest.depotID
        manifestGID = manifest.manifestGID
        creationTime = manifest.creationTime
        totalUncompressedSize = manifest.totalUncompressedSize
        totalCompressedSize = manifest.totalCompressedSize
        encryptedCRC = manifest.encryptedCRC

        manifest.mapping.forEach { fileMapping ->
            val filedata = FileData(
                filename = fileMapping.fileName!!,
                filenameHash = fileMapping.hashFileName!!,
                flag = fileMapping.flags,
                size = fileMapping.totalSize,
                hash = fileMapping.hashContent!!,
                linkTarget = "",
                encrypted = filenamesEncrypted,
                numChunks = fileMapping.chunks.size
            )

            fileMapping.chunks.forEach { chunk ->
                val chunkData = ChunkData(
                    chunkID = chunk.chunkGID!!,
                    checksum = chunk.checksum,
                    offset = chunk.offset,
                    compressedLength = chunk.compressedSize,
                    uncompressedLength = chunk.decompressedSize,
                )
                filedata.chunks.add(chunkData)
            }

            files.add(filedata)
        }
    }

    private fun parseProtobufManifestPayload(payload: ContentManifestPayload) {
        files = ArrayList(payload.mappingsCount)

        payload.mappingsList.forEach { fileMapping ->
            val filedata = FileData(
                filename = fileMapping.filename,
                filenameHash = fileMapping.shaFilename.toByteArray(),
                flag = EDepotFileFlag.from(fileMapping.flags),
                size = fileMapping.size,
                hash = fileMapping.shaContent.toByteArray(),
                linkTarget = fileMapping.linktarget,
                encrypted = filenamesEncrypted,
                numChunks = fileMapping.chunksList.size,
            )

            fileMapping.chunksList.forEach { chunk ->
                val chunkData = ChunkData(
                    chunkID = chunk.sha.toByteArray(),
                    checksum = chunk.crc,
                    offset = chunk.offset,
                    compressedLength = chunk.cbCompressed,
                    uncompressedLength = chunk.cbOriginal
                )
                filedata.chunks.add(chunkData)
            }

            files.add(filedata)
        }
    }

    private fun parseProtobufManifestMetadata(metadata: ContentManifestMetadata) {
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
    fun serialize(output: OutputStream) {
        // Basically like ChunkIdComparer from DepotDownloader
        class ByteArrayKey(private val byteArray: ByteArray) {
            override fun equals(other: Any?): Boolean = other is ByteArrayKey && byteArray.contentEquals(other.byteArray)

            override fun hashCode(): Int = if (byteArray.size >= 4) {
                ((byteArray[0].toInt() and 0xFF)) or
                    ((byteArray[1].toInt() and 0xFF) shl 8) or
                    ((byteArray[2].toInt() and 0xFF) shl 16) or
                    ((byteArray[3].toInt() and 0xFF) shl 24)
            } else {
                byteArray.contentHashCode()
            }
        }

        val payload = ContentManifestPayload.newBuilder()
        val uniqueChunks = hashSetOf<ByteArrayKey>()

        files.forEach { file ->
            val protofile = ContentManifestPayload.FileMapping.newBuilder().apply {
                this.size = file.totalSize
                this.flags = EDepotFileFlag.code(file.flags)
            }

            if (filenamesEncrypted) {
                // Assume the name is unmodified
                protofile.filename = file.fileName
                protofile.shaFilename = ByteString.copyFrom(file.fileNameHash)
            } else {
                protofile.filename = file.fileName.replace('/', '\\')
                protofile.shaFilename = ByteString.copyFrom(
                    CryptoHelper.shaHash(
                        file.fileName
                            .replace('/', '\\')
                            .lowercase(Locale.getDefault())
                            .toByteArray(Charsets.UTF_8)
                    )
                )
            }

            protofile.shaContent = ByteString.copyFrom(file.fileHash)

            if (!file.linkTarget.isNullOrBlank()) {
                protofile.linktarget = file.linkTarget
            }

            file.chunks.forEach { chunk ->
                val protochunk = ContentManifestPayload.FileMapping.ChunkData.newBuilder().apply {
                    this.sha = ByteString.copyFrom(chunk.chunkID)
                    this.crc = chunk.checksum
                    this.offset = chunk.offset
                    this.cbOriginal = chunk.uncompressedLength
                    this.cbCompressed = chunk.compressedLength
                }.build()

                protofile.addChunks(protochunk)
                uniqueChunks.add(ByteArrayKey(chunk.chunkID!!))
            }

            payload.addMappings(protofile.build())
        }

        val metadata = ContentManifestMetadata.newBuilder().apply {
            this.depotId = depotID
            this.gidManifest = manifestGID
            this.creationTime = (this@DepotManifest.creationTime.time / 1000).toInt()
            this.filenamesEncrypted = this@DepotManifest.filenamesEncrypted
            this.cbDiskOriginal = totalUncompressedSize
            this.cbDiskCompressed = totalCompressedSize
            this.uniqueChunks = uniqueChunks.size
        }

        // Calculate payload CRC
        val msPayload = MemoryStream()
        payload.build().writeTo(msPayload.asOutputStream())

        val len = msPayload.length.toInt()
        val data = ByteArray(4 + len)

        // Alternative of BitConverter.GetBytes(len)
        val lenBytes = ByteArray(4)
        lenBytes[0] = (len and 0xFF).toByte()
        lenBytes[1] = ((len shr 8) and 0xFF).toByte()
        lenBytes[2] = ((len shr 16) and 0xFF).toByte()
        lenBytes[3] = ((len shr 24) and 0xFF).toByte()

        System.arraycopy(lenBytes, 0, data, 0, 4)
        System.arraycopy(msPayload.toByteArray(), 0, data, 4, len)
        val crc32 = Utils.crc32(data).toInt()

        msPayload.close()

        if (filenamesEncrypted) {
            metadata.crcEncrypted = crc32
            metadata.crcClear = 0
        } else {
            metadata.crcEncrypted = encryptedCRC
            metadata.crcClear = crc32
        }

        val bw = BinaryWriter(output)

        // Write Protobuf payload
        val payloadBytes = payload.build().toByteArray()
        bw.writeInt(PROTOBUF_PAYLOAD_MAGIC)
        bw.writeInt(payloadBytes.size)
        bw.write(payloadBytes)

        // Write Protobuf metadata
        val metadataBytes = metadata.build().toByteArray()
        bw.writeInt(PROTOBUF_METADATA_MAGIC)
        bw.writeInt(metadataBytes.size)
        bw.write(metadataBytes)

        // Write empty signature section
        bw.writeInt(PROTOBUF_SIGNATURE_MAGIC)
        bw.writeInt(0)

        // Write EOF marker
        bw.writeInt(PROTOBUF_ENDOFMANIFEST_MAGIC)

        bw.close()
    }
}
