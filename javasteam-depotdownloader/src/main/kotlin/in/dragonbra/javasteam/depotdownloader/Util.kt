package `in`.dragonbra.javasteam.depotdownloader

import `in`.dragonbra.javasteam.depotdownloader.data.DepotDownloadInfo
import `in`.dragonbra.javasteam.enums.EDepotFileFlag
import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.util.Adler32
import okio.FileHandle
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.apache.commons.lang3.SystemUtils
import java.io.IOException
import java.security.MessageDigest

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
object Util {

    @JvmOverloads
    @JvmStatic
    fun getSteamOS(androidEmulation: Boolean = false): String {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "windows"
        }
        if (SystemUtils.IS_OS_MAC_OSX) {
            return "macos"
        }
        if (SystemUtils.IS_OS_LINUX) {
            return "linux"
        }
        if (SystemUtils.IS_OS_FREE_BSD) {
            // Return linux as freebsd steam client doesn't exist yet
            return "linux"
        }

        // Hack for PC emulation on android. (Pluvia, GameNative, GameHub)
        if (androidEmulation && SystemUtils.IS_OS_ANDROID) {
            return "windows"
        }

        return "unknown"
    }

    @JvmStatic
    fun getSteamArch(): String {
        val arch = System.getProperty("os.arch")?.lowercase() ?: ""
        return when {
            arch.contains("64") -> "64"
            arch.contains("86") -> "32"
            arch.contains("amd64") -> "64"
            arch.contains("x86_64") -> "64"
            arch.contains("aarch64") -> "64"
            arch.contains("arm64") -> "64"
            else -> "32"
        }
    }

    @JvmStatic
    fun saveManifestToFile(directory: Path, manifest: DepotManifest): Boolean = try {
        val filename = directory / "${manifest.depotID}_${manifest.manifestGID}.manifest"
        manifest.saveToFile(filename.toString())

        val shaFile = "$filename.sha".toPath()
        FileSystem.SYSTEM.write(shaFile) {
            write(fileSHAHash(filename))
        }

        true
    } catch (e: Exception) {
        false
    }

    @JvmStatic
    fun loadManifestFromFile(
        directory: Path,
        depotId: Int,
        manifestId: Long,
        badHashWarning: Boolean,
    ): DepotManifest? {
        // Try loading Steam format manifest first.
        val filename = directory / "${depotId}_$manifestId.manifest"

        if (FileSystem.SYSTEM.exists(filename)) {
            val expectedChecksum = try {
                FileSystem.SYSTEM.read(filename / ".sha") {
                    readByteArray()
                }
            } catch (e: IOException) {
                null
            }

            val currentChecksum = fileSHAHash(filename)

            if (expectedChecksum != null && expectedChecksum.contentEquals(currentChecksum)) {
                return DepotManifest.loadFromFile(filename.toString())
            } else if (badHashWarning) {
                println("Manifest $manifestId on disk did not match the expected checksum.")
            }
        }

        return null
    }

    @JvmStatic
    fun fileSHAHash(filename: Path): ByteArray {
        val digest = MessageDigest.getInstance("SHA-1")

        FileSystem.SYSTEM.source(filename).use { source ->
            source.buffer().use { bufferedSource ->
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (bufferedSource.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
        }

        return digest.digest()
    }

    /**
     * Validate a file against Steam3 Chunk data
     *
     * @param handle FileHandle to read from
     * @param chunkData Array of ChunkData to validate against
     * @return List of ChunkData that are needed
     * @throws IOException If there's an error reading the file
     */
    @Throws(IOException::class)
    fun validateSteam3FileChecksums(handle: FileHandle, chunkData: List<ChunkData>): List<ChunkData> {
        val neededChunks = mutableListOf<ChunkData>()

        for (data in chunkData) {
            val chunk = ByteArray(data.uncompressedLength)
            val read = handle.read(data.offset, chunk, 0, data.uncompressedLength)

            val tempChunk = if (read > 0 && read < data.uncompressedLength) {
                chunk.copyOf(read)
            } else {
                chunk
            }

            val adler = Adler32.calculate(tempChunk)
            if (adler != data.checksum) {
                neededChunks.add(data)
            }
        }

        return neededChunks
    }

    @JvmStatic
    fun dumpManifestToTextFile(depot: DepotDownloadInfo, manifest: DepotManifest) {
        val txtManifest = depot.installDir / "manifest_${depot.depotId}_${depot.manifestId}.txt"

        FileSystem.SYSTEM.write(txtManifest) {
            writeUtf8("Content Manifest for Depot ${depot.depotId}\n")
            writeUtf8("\n")
            writeUtf8("Manifest ID / date     : ${depot.manifestId} / ${manifest.creationTime}\n")

            val uniqueChunks = manifest.files
                .flatMap { it.chunks }
                .mapNotNull { it.chunkID }
                .toSet()

            writeUtf8("Total number of files  : ${manifest.files.size}\n")
            writeUtf8("Total number of chunks : ${uniqueChunks.size}\n")
            writeUtf8("Total bytes on disk    : ${manifest.totalUncompressedSize}\n")
            writeUtf8("Total bytes compressed : ${manifest.totalCompressedSize}\n")
            writeUtf8("\n")
            writeUtf8("\n")

            writeUtf8("          Size Chunks File SHA                                 Flags Name\n")
            manifest.files.forEach { file ->
                val sha1Hash = file.fileHash.toHexString().lowercase()
                writeUtf8(
                    "%14d %6d %s %5x %s\n".format(
                        file.totalSize,
                        file.chunks.size,
                        sha1Hash,
                        EDepotFileFlag.code(file.flags),
                        file.fileName
                    )
                )
            }
        }
    }

    @JvmStatic
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "%.2f %s".format(size, units[unitIndex])
    }
}
