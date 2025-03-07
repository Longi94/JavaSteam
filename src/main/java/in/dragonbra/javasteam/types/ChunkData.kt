package `in`.dragonbra.javasteam.types

/**
 * Represents a single chunk within a file.
 */
class ChunkData {

    /**
     * Gets or sets the SHA-1 hash chunk id.
     */
    var chunkID: ByteArray? = null

    /**
     * Gets or sets the expected Adler32 checksum of this chunk.
     */
    var checksum: Int = 0

    /**
     * Gets or sets the chunk offset.
     */
    var offset: Long = 0

    /**
     * Gets or sets the compressed length of this chunk.
     */
    var compressedLength: Int = 0

    /**
     * Gets or sets the decompressed length of this chunk.
     */
    var uncompressedLength: Int = 0

    /**
     * Initializes a new instance of the ChunkData class.
     */
    constructor()

    /**
     * Initializes a new instance of the [ChunkData] class with specified values.
     */
    constructor(id: ByteArray, checksum: Int, offset: Long, compLength: Int, uncompLength: Int) {
        this.chunkID = id
        this.checksum = checksum
        this.offset = offset
        this.compressedLength = compLength
        this.uncompressedLength = uncompLength
    }

    /**
     * Internal constructor helper
     */
    constructor(chunkData: ChunkData) {
        chunkID = chunkData.chunkID
        checksum = chunkData.checksum
        offset = chunkData.offset
        compressedLength = chunkData.compressedLength
        uncompressedLength = chunkData.uncompressedLength
    }
}
