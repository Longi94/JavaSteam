package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.EnumSet

/**
 * Represents the binary Steam3 manifest format.
 */
@Suppress("unused")
class Steam3Manifest {

    companion object {
        const val MAGIC: Int = 0x16349781
        const val CURRENT_VERSION: Int = 4
    }

    class FileMapping {

        @Suppress("ArrayInDataClass")
        data class Chunk(
            var chunkGID: ByteArray? = null, // sha1 hash for this chunk
            var checksum: Int = 0,
            var offset: Long = 0L,
            var decompressedSize: Int = 0,
            var compressedSize: Int = 0,
        ) {
            internal fun deserialize(ds: BinaryReader) {
                chunkGID = ds.readBytes(20)
                checksum = ds.readInt()
                offset = ds.readLong()
                decompressedSize = ds.readInt()
                compressedSize = ds.readInt()
            }
        }

        var fileName: String? = null

        var totalSize: Long = 0L
        var flags: EnumSet<EDepotFileFlag> = EnumSet.noneOf(EDepotFileFlag::class.java)

        var hashFileName: ByteArray? = null
        var hashContent: ByteArray? = null

        var numChunks: Int = 0
        var chunks: ArrayList<Chunk>? = null

        internal fun deserialize(ds: BinaryReader) {
            fileName = ds.readNullTermString(StandardCharsets.UTF_8)

            totalSize = ds.readLong()

            flags = EDepotFileFlag.from(ds.readInt())

            hashContent = ds.readBytes(20)
            hashFileName = ds.readBytes(20)

            numChunks = ds.readInt()

            chunks = ArrayList<Chunk>(numChunks)

            for (i in 0 until chunks!!.size) {
                chunks!![i] = Chunk().apply {
                    deserialize(ds)
                }
            }
        }
    }

    var magic: Int = 0
    var version: Int = 0
    var depotID: Int = 0
    var manifestGID: Long = 0L
    var creationTime: Date = Date()
    var areFileNamesEncrypted: Boolean = false
    var totalUncompressedSize: Long = 0L
    var totalCompressedSize: Long = 0L
    var chunkCount: Int = 0
    var fileEntryCount: Int = 0
    var fileMappingSize: Int = 0
    var encryptedCRC: Int = 0
    var decryptedCRC: Int = 0
    var flags: Int = 0
    var mapping: ArrayList<FileMapping>? = null

    internal fun deserialize(ds: BinaryReader) {
        // The magic is verified by DepotManifest.InternalDeserialize, not checked here to avoid seeking
        // Magic = ds.readInt();
        // if (Magic != MAGIC) {
        //     throw new InvalidDataException("data is not a valid steam3 manifest: incorrect magic.");
        // }

        version = ds.readInt()

        if (version != CURRENT_VERSION) {
            throw IllegalArgumentException("Only version $CURRENT_VERSION is supported.")
        }

        depotID = ds.readInt()

        manifestGID = ds.readLong()
        creationTime = Date.from(Instant.ofEpochSecond(ds.readInt().toLong()))

        areFileNamesEncrypted = ds.readInt() != 0

        totalUncompressedSize = ds.readLong()
        totalCompressedSize = ds.readLong()

        chunkCount = ds.readInt()

        fileEntryCount = ds.readInt()
        fileMappingSize = ds.readInt()

        mapping = ArrayList<FileMapping>(fileMappingSize)

        encryptedCRC = ds.readInt()
        decryptedCRC = ds.readInt()

        flags = ds.readInt()

        var i = fileMappingSize
        while (i > 0) {
            val start = ds.position

            val fileMapping = FileMapping().apply { deserialize(ds) }
            mapping!!.add(fileMapping)

            i -= (ds.position - start)
        }
    }
}
