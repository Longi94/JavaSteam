package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import java.io.File
import java.util.EnumSet

/**
 * Represents a single file within a manifest.
 *
 * @constructor Initializes a new instance of the [FileData] class.
 *
 * @param fileName Gets the name of the file.
 * @param fileNameHash Gets SHA-1 hash of this file's name.
 * @param chunks Gets the chunks that this file is composed of.
 * @param flags Gets the file flags
 * @param totalSize Gets the total size of this file.
 * @param fileHash Gets SHA-1 hash of this file.
 * @param linkTarget Gets symlink target of this file.
 */
data class FileData(
    var fileName: String = "",
    var fileNameHash: ByteArray = byteArrayOf(),
    var chunks: MutableList<ChunkData> = mutableListOf(),
    var flags: EnumSet<EDepotFileFlag> = EnumSet.noneOf(EDepotFileFlag::class.java),
    var totalSize: Long = 0,
    var fileHash: ByteArray = byteArrayOf(),
    var linkTarget: String? = null,
) {
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
    ) : this(
        fileName = if (encrypted) filename else filename.replace('\\', File.separatorChar),
        fileNameHash = filenameHash,
        chunks = ArrayList(numChunks),
        flags = flag,
        totalSize = size,
        fileHash = hash,
        linkTarget = linkTarget,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileData

        if (totalSize != other.totalSize) return false
        if (fileName != other.fileName) return false
        if (!fileNameHash.contentEquals(other.fileNameHash)) return false
        if (chunks != other.chunks) return false
        if (flags != other.flags) return false
        if (!fileHash.contentEquals(other.fileHash)) return false
        if (linkTarget != other.linkTarget) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalSize.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileNameHash.contentHashCode()
        result = 31 * result + chunks.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + fileHash.contentHashCode()
        result = 31 * result + (linkTarget?.hashCode() ?: 0)
        return result
    }
}
