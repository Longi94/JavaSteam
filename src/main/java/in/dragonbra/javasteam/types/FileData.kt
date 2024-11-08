package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import java.io.File
import java.util.EnumSet

/**
 * Represents a single file within a manifest.
 */
class FileData {

    /**
     * Gets the name of the file.
     */
    var fileName: String
        internal set

    /**
     * Gets SHA-1 hash of this file's name.
     */
    val fileNameHash: ByteArray

    /**
     * Gets the chunks that this file is composed of.
     */
    val chunks: MutableList<ChunkData>

    /**
     * Gets the file flags
     */
    val flags: EnumSet<EDepotFileFlag>

    /**
     * Gets the total size of this file.
     */
    val totalSize: Long

    /**
     * Gets SHA-1 hash of this file.
     */
    val fileHash: ByteArray

    /**
     * Gets symlink target of this file.
     */
    val linkTarget: String

    constructor(
        fileName: String,
        fileNameHash: ByteArray,
        chunks: MutableList<ChunkData> = mutableListOf(),
        flags: EnumSet<EDepotFileFlag>,
        totalSize: Long,
        fileHash: ByteArray,
        linkTarget: String,
        encrypted: Boolean,
    ) {
        if (encrypted) {
            this.fileName = fileName
        } else {
            this.fileName = fileName.replace('\\', File.separatorChar)
        }
        this.fileNameHash = fileNameHash
        this.chunks = chunks
        this.flags = flags
        this.totalSize = totalSize
        this.fileHash = fileHash
        this.linkTarget = linkTarget
    }

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
