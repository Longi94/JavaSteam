package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import java.io.File
import java.util.EnumSet

/**
 * Represents a single file within a manifest.
 *
 * @constructor Initializes a new instance of the [FileData] class.
 */
@Suppress("unused")
class FileData {

    /**
     * Gets the name of the file.
     */
    var fileName: String = ""

    /**
     * Gets SHA-1 hash of this file's name.
     */
    var fileNameHash: ByteArray = byteArrayOf()

    /**
     * Gets the chunks that this file is composed of.
     */
    var chunks: MutableList<ChunkData> = mutableListOf()

    /**
     * Gets the file flags
     */
    var flags: EnumSet<EDepotFileFlag> = EnumSet.noneOf(EDepotFileFlag::class.java)

    /**
     * Gets the total size of this file.
     */
    var totalSize: Long = 0

    /**
     * Gets SHA-1 hash of this file.
     */
    var fileHash: ByteArray = byteArrayOf()

    /**
     * Gets symlink target of this file.
     */
    var linkTarget: String? = null

    /**
     * Initializes a new instance of the [FileData] class with specified values.
     */
    constructor(
        filename: String,
        filenameHash: ByteArray,
        flag: EnumSet<EDepotFileFlag>,
        size: Long,
        hash: ByteArray,
        linkTarget: String,
        encrypted: Boolean,
        numChunks: Int,
    ) {
        this.fileName = if (encrypted) filename else filename.replace('\\', File.separatorChar)
        this.fileNameHash = filenameHash
        this.flags = flag
        this.totalSize = size
        this.fileHash = hash
        this.chunks = ArrayList(numChunks)
        this.linkTarget = linkTarget
    }

    /**
     * Internal constructor helper
     */
    constructor(fileData: FileData) {
        fileName = fileData.fileName
        fileNameHash = fileData.fileNameHash
        chunks = fileData.chunks.map { ChunkData(it) }.toMutableList()
        flags = fileData.flags
        totalSize = fileData.totalSize
        fileHash = fileData.fileHash
        linkTarget = fileData.linkTarget
    }
}
