package `in`.dragonbra.javasteam.types

/**
 * Represents a single chunk within a file.
 */
class ChunkData {

    /**
     * Gets or sets the SHA-1 hash chunk id.
     */
    val chunkID: ByteArray?
    /**
     * Gets or sets the expected Adler32 checksum of this chunk.
     */
    val checksum: Int
    /**
     * Gets or sets the chunk offset.
     */
    val offset: Long

    /**
     * Gets or sets the compressed length of this chunk.
     */
    val compressedLength: Int
    /**
     * Gets or sets the decompressed length of this chunk.
     */
    val uncompressedLength: Int

    constructor(
        chunkID: ByteArray?,
        checksum: Int,
        offset: Long,
        compressedLength: Int,
        uncompressedLength: Int,
    ) {
        this.chunkID = chunkID
        this.checksum = checksum
        this.offset = offset
        this.compressedLength = compressedLength
        this.uncompressedLength = uncompressedLength
    }
    constructor(chunkData: ChunkData) {
        chunkID = chunkData.chunkID
        checksum = chunkData.checksum
        offset = chunkData.offset
        compressedLength = chunkData.compressedLength
        uncompressedLength = chunkData.uncompressedLength
    }
}
