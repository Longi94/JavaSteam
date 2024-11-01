package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManifestProvider(private val file: File) : IManifestProvider {
    companion object {
        private val logger: Logger = LogManager.getLogger(FileManifestProvider::class.java)
    }

    /**
     * Instantiates a [FileManifestProvider] object.
     * @param file the file that will store the depot manifests
     */
    init {
        try {
            file.absoluteFile.parentFile?.mkdirs()
            file.createNewFile()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    override fun fetchManifest(depotID: Int, manifestID: Long): Pair<DepotManifest, ByteArray>? {
        return ZipInputStream(FileInputStream(file)).use { zip ->
            seekToEntry(zip, "$depotID${File.separator}$manifestID")?.let {
                if (it.size > 0) {
                    DepotManifest.deserialize(zip)
                } else {
                    null
                }
            }
        }
    }

    override fun fetchLatestManifest(depotID: Int): Pair<DepotManifest, ByteArray>? {
        return ZipInputStream(FileInputStream(file)).use { zip ->
            seekToEntry(zip, "$depotID${File.separator}latest")?.let { idEntry ->
                if (idEntry.size > 0) {
                    val manifestId = ByteBuffer.wrap(zip.readNBytes(idEntry.size.toInt())).getLong()
                    zip.reset()
                    seekToEntry(zip, "$depotID${File.separator}$manifestId")?.let {
                        if (it.size > 0) {
                            DepotManifest.deserialize(zip)
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            }
        }
    }

    override fun setLatestManifestId(depotID: Int, manifestID: Long) {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("${depotID}${File.separator}latest"))
            val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
            buffer.putLong(manifestID)
            zip.write(buffer.array())
//            zip.closeEntry()
        }
    }

    override fun updateManifest(manifest: DepotManifest): ByteArray {
        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("${manifest.depotID}${File.separator}${manifest.manifestGID}"))
            val checksum = manifest.serialize(zip)
//            zip.closeEntry()
            return checksum
        }
    }

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
}
