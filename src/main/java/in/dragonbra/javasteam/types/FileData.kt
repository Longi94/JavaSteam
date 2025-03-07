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
@Suppress("ArrayInDataClass")
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
}
