package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.EnumSet

/**
 * Represents the binary Steam3 manifest format.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Steam3Manifest {

    companion object {
        const val MAGIC: Int = 0x16349781
        private const val CURRENT_VERSION: Int = 4
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class FileMapping {
        class Chunk {
            var chunkGID: ByteArray? = null // sha1 hash for this chunk
            var checksum: Int = 0
            var offset: Long = 0L
            var decompressedSize: Int = 0
            var compressedSize: Int = 0

            internal fun deserialize(ds: BinaryReader) {
                chunkGID = ds.readBytes(20)
                checksum = ds.readInt()
                offset = ds.readLong()
                decompressedSize = ds.readInt()
                compressedSize = ds.readInt()
            }
        }

        var fileName: String? = null
        var totalSize: Long = 0
        var flags: EnumSet<EDepotFileFlag> = EnumSet.noneOf(EDepotFileFlag::class.java)
        var hashFileName: ByteArray? = null
        var hashContent: ByteArray? = null
        var numChunks: Int = 0
        var chunks: Array<Chunk> = arrayOf()
            private set

        internal fun deserialize(ds: BinaryReader) {
            fileName = ds.readNullTermString(StandardCharsets.UTF_8)
            totalSize = ds.readLong()
            flags = EDepotFileFlag.from(ds.readInt())
            hashContent = ds.readBytes(20)
            hashFileName = ds.readBytes(20)
            numChunks = ds.readInt()
            chunks = Array(numChunks) { Chunk() }

            for (x in chunks.indices) {
                chunks[x].deserialize(ds)
            }
        }
    }

    var magic: Int = 0
    var version: Int = 0
    var depotID: Int = 0
    var manifestGID: Long = 0
    var creationTime: Date = Date()
    var areFileNamesEncrypted: Boolean = false
    var totalUncompressedSize: Long = 0
    var totalCompressedSize: Long = 0
    var chunkCount: Int = 0
    var fileEntryCount: Int = 0
    var fileMappingSize: Int = 0
    var encryptedCRC: Int = 0
    var decryptedCRC: Int = 0
    var flags: Int = 0
    var mapping: MutableList<FileMapping> = mutableListOf()

    internal fun deserialize(ds: BinaryReader) {
        // The magic is verified by DepotManifest.InternalDeserialize, not checked here to avoid seeking
        // magic = ds.readInt()
        // if (magic != MAGIC) {
        //     throw IOException("data is not a valid steam3 manifest: incorrect magic.")
        // }

        version = ds.readInt()

        if (version != CURRENT_VERSION) {
            throw NotImplementedError("Only version $CURRENT_VERSION is supported.")
        }

        depotID = ds.readInt()
        manifestGID = ds.readLong()
        creationTime = Date(ds.readInt() * 1000L)
        areFileNamesEncrypted = ds.readInt() != 0
        totalUncompressedSize = ds.readLong()
        totalCompressedSize = ds.readLong()
        chunkCount = ds.readInt()
        fileEntryCount = ds.readInt()
        fileMappingSize = ds.readInt()

        mapping = ArrayList(fileMappingSize)

        encryptedCRC = ds.readInt()
        decryptedCRC = ds.readInt()
        flags = ds.readInt()

        var i = fileMappingSize
        while (i > 0) {
            val start = ds.position.toLong()

            val fileMapping = FileMapping()
            fileMapping.deserialize(ds)
            mapping.add(fileMapping)

            i -= (ds.position - start.toInt())
        }
    }
}
