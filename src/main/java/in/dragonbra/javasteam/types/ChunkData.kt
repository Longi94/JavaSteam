package `in`.dragonbra.javasteam.types

/**
 * Represents a single chunk within a file.
 *
 * @constructor Initializes a new instance of the [ChunkData] class.
 * @constructor Initializes a new instance of the [ChunkData] class with specified values.
 *
 * @param chunkID Gets or sets the SHA-1 hash chunk id.
 * @param checksum Gets or sets the expected Adler32 checksum of this chunk.
 * @param offset Gets or sets the chunk offset.
 * @param compressedLength Gets or sets the compressed length of this chunk.
 * @param uncompressedLength Gets or sets the decompressed length of this chunk.
 */
@Suppress("ArrayInDataClass")
data class ChunkData(
    var chunkID: ByteArray? = null,
    var checksum: Int = 0,
    var offset: Long = 0,
    var compressedLength: Int = 0,
    var uncompressedLength: Int = 0,
)
