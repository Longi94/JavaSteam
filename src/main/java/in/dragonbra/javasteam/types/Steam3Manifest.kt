@file:Suppress("unused")

package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.lang.Exception
import java.time.Instant
import java.util.Date
import java.util.EnumSet

/**
 * Represents the binary Steam3 manifest format.
 */
class Steam3Manifest(
//    val magic: Int,
    val version: Int,
    val depotID: Int,
    val manifestGID: Long,
    val creationTime: Date,
    val areFileNamesEncrypted: Boolean,
    val totalUncompressedSize: Long,
    val totalCompressedSize: Long,
    val chunkCount: Int,
    val fileEntryCount: Int,
    val fileMappingSize: Int,
    val encryptedCRC: Int,
    val decryptedCRC: Int,
    val flags: Int,
    val fileMapping: List<FileMapping>,
) {

    class FileMapping(
        val fileName: String,
        val totalSize: Long,
        val flags: EnumSet<EDepotFileFlag>,
        val hashFileName: ByteArray,
        val hashContent: ByteArray,
        val numChunks: Int,
        val chunks: Array<Chunk>,
    ) {

        class Chunk(
            val chunkGID: ByteArray, // sha1 hash for this chunk
            val checksum: Int,
            val offset: Long,
            val decompressedSize: Int,
            val compressedSize: Int,
        ) {
            companion object {
                internal fun deserialize(ds: BinaryReader): Chunk = Chunk(
                    chunkGID = ds.readNBytesCompat(20),
                    checksum = ds.readInt(),
                    offset = ds.readLong(),
                    decompressedSize = ds.readInt(),
                    compressedSize = ds.readInt()
                )
            }
        }

        companion object {
            internal fun deserialize(ds: BinaryReader): FileMapping {
                val fileName = ds.readNullTermString(Charsets.UTF_8)
                val totalSize = ds.readLong()
                val flags = EDepotFileFlag.from(ds.readInt())
                val hashContent = ds.readNBytesCompat(20)
                val hashFileName = ds.readNBytesCompat(20)
                val numChunks = ds.readInt()

                return FileMapping(
                    fileName = fileName,
                    totalSize = totalSize,
                    flags = flags,
                    hashContent = hashContent,
                    hashFileName = hashFileName,
                    numChunks = numChunks,
                    chunks = Array(numChunks) { Chunk.deserialize(ds) }
                )
            }
        }
    }

    companion object {
        const val MAGIC: Int = 0x16349781
        const val CURRENT_VERSION: Int = 4

        internal fun deserialize(ds: BinaryReader): Steam3Manifest {
            // The magic is verified by DepotManifest.InternalDeserialize, not checked here to avoid seeking
            val version = ds.readInt()

            if (version != CURRENT_VERSION) {
                // Not Implemented Exception
                throw Exception("Only version $CURRENT_VERSION is supported")
            }

            val depotID = ds.readInt()
            val manifestGID = ds.readLong()
            val creationTime = Date.from(Instant.ofEpochSecond(ds.readInt().toLong()))
            val areFileNamesEncrypted = ds.readInt() != 0
            val totalUncompressedSize = ds.readLong()
            val totalCompressedSize = ds.readLong()
            val chunkCount = ds.readInt()
            val fileEntryCount = ds.readInt()
            val fileMappingSize = ds.readInt()
            val encryptedCRC = ds.readInt()
            val decryptedCRC = ds.readInt()
            val flags = ds.readInt()

            val fileMapping = mutableListOf<FileMapping>()
            var size = fileMappingSize

            while (size > 0) {
                val start = ds.position
                fileMapping.add(FileMapping.deserialize(ds))
                size -= ds.position - start
            }

            return Steam3Manifest(
                version = version,
                depotID = depotID,
                manifestGID = manifestGID,
                creationTime = creationTime,
                areFileNamesEncrypted = areFileNamesEncrypted,
                totalUncompressedSize = totalUncompressedSize,
                totalCompressedSize = totalCompressedSize,
                chunkCount = chunkCount,
                fileEntryCount = fileEntryCount,
                fileMappingSize = fileMappingSize,
                fileMapping = fileMapping,
                encryptedCRC = encryptedCRC,
                decryptedCRC = decryptedCRC,
                flags = flags,
            )
        }
    }
}
