package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Depot manifest provider that stores depot manifests in a zip file.
 * @constructor Instantiates a [FileManifestProvider] object.
 * @param file the file that will store the depot manifests
 *
 * @author Oxters
 * @since 2024-11-07
 */
@Suppress("unused")
class FileManifestProvider(private val file: Path) : IManifestProvider {

    /**
     * Instantiates a [FileManifestProvider] object.
     * @param file the file that will store the depot manifests.
     */
    constructor(file: File) : this(file.toPath())

    /**
     * Instantiates a [FileManifestProvider] object.
     * @param filename the filename that will store the depot manifests.
     */
    constructor(filename: String) : this(Path.of(filename))

    init {
        require(file.fileName.toString().isNotBlank()) { "FileName must not be blank" }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(FileManifestProvider::class.java)

        private fun getLatestEntryName(depotID: Int): String = "$depotID${File.separator}latest"
        private fun getEntryName(depotID: Int, manifestID: Long): String = "$depotID${File.separator}$manifestID.bin"

        private fun seekToEntry(zipStream: ZipInputStream, entryName: String): ZipEntry? {
            var zipEntry: ZipEntry?

            do {
                zipEntry = zipStream.nextEntry
                if (zipEntry?.name.equals(entryName, true)) {
                    break
                }
            } while (zipEntry != null)

            return zipEntry
        }

        private fun copyZip(from: ZipInputStream, to: ZipOutputStream, vararg excludeEntries: String) {
            var entry = from.nextEntry

            while (entry != null) {
                if (!excludeEntries.contains(entry.name) && (entry.isDirectory || (!entry.isDirectory && entry.size > 0))) {
                    to.putNextEntry(entry)

                    if (!entry.isDirectory) {
                        val entryBytes = ByteArray(entry.size.toInt())

                        from.readNBytes(entryBytes, 0, entryBytes.size)
                        to.write(entryBytes)
                    }

                    to.closeEntry()
                }

                entry = from.nextEntry
            }
        }

        private fun zipUncompressed(zip: ZipOutputStream, entryName: String, bytes: ByteArray) {
            val entry = ZipEntry(entryName)
            entry.method = ZipEntry.STORED
            entry.size = bytes.size.toLong()
            entry.compressedSize = bytes.size.toLong()
            entry.crc = CRC32().run {
                update(bytes)
                value
            }

            zip.putNextEntry(entry)
            zip.write(bytes)
            zip.closeEntry()
        }
    }

    override fun fetchManifest(depotID: Int, manifestID: Long): DepotManifest? = runCatching {
        Files.newInputStream(file).use { fis ->
            ZipInputStream(fis).use { zip ->
                seekToEntry(zip, getEntryName(depotID, manifestID))?.let {
                    if (it.size > 0) {
                        DepotManifest.deserialize(zip)
                    } else {
                        null
                    }
                }
            }
        }
    }.fold(
        onSuccess = { it },
        onFailure = { error ->
            when (error) {
                is NoSuchFileException -> logger.debug("File doesn't exist")
                else -> logger.error("Unknown error occurred", error)
            }

            null
        }
    )

    override fun fetchLatestManifest(depotID: Int): DepotManifest? = runCatching {
        Files.newInputStream(file).use { fis ->
            ZipInputStream(fis).use { zip ->
                seekToEntry(zip, getLatestEntryName(depotID))?.let { idEntry ->
                    if (idEntry.size > 0) {
                        ByteBuffer.wrap(zip.readNBytes(idEntry.size.toInt())).getLong()
                    } else {
                        null
                    }
                }
            }
        }?.let { manifestId ->
            fetchManifest(depotID, manifestId)
        }
    }.fold(
        onSuccess = { it },
        onFailure = { error ->
            when (error) {
                is NoSuchFileException -> logger.debug("File doesn't exist")
                else -> logger.error("Unknown error occurred", error)
            }

            null
        }
    )

    override fun setLatestManifestId(depotID: Int, manifestID: Long) {
        ByteArrayOutputStream().use { bs ->
            ZipOutputStream(bs).use { zip ->
                // copy old file only if it exists
                if (Files.exists(file)) {
                    Files.newInputStream(file).use { fis ->
                        ZipInputStream(fis).use { zs ->
                            copyZip(zs, zip, getLatestEntryName(depotID))
                        }
                    }
                }
                // write manifest id as uncompressed data
                ByteBuffer.allocate(Long.SIZE_BYTES).apply {
                    putLong(manifestID)
                    zipUncompressed(zip, getLatestEntryName(depotID), array())
                }
            }
            // save all data to the file
            try {
                Files.newOutputStream(file).use { fos ->
                    fos.write(bs.toByteArray())
                }
            } catch (e: IOException) {
                logger.error("Failed to write manifest ID to file ${file.fileName}", e)
            }
        }
    }

    override fun updateManifest(manifest: DepotManifest) {
        ByteArrayOutputStream().use { bs ->
            ZipOutputStream(bs).use { zip ->
                // copy old file only if it exists
                if (Files.exists(file)) {
                    Files.newInputStream(file).use { fis ->
                        ZipInputStream(fis).use { zs ->
                            copyZip(zs, zip, getEntryName(manifest.depotID, manifest.manifestGID))
                        }
                    }
                }
                // add manifest as uncompressed data
                zipUncompressed(zip, getEntryName(manifest.depotID, manifest.manifestGID), manifest.toByteArray())
            }
            // save all data to the file
            try {
                Files.newOutputStream(file).use { fos ->
                    fos.write(bs.toByteArray())
                }
            } catch (e: IOException) {
                logger.error("Failed to write manifest to file ${file.fileName}", e)
            }
        }
    }
}
