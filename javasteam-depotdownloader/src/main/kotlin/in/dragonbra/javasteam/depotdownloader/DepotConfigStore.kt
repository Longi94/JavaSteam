package `in`.dragonbra.javasteam.depotdownloader

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * @author Lossy
 * @since Oct 1, 2025
 */
@Serializable
data class DepotConfigStore(
    val installedManifestIDs: MutableMap<Int, Long> = mutableMapOf(),
) {
    companion object {
        private var instance: DepotConfigStore? = null

        private var filePath: Path? = null

        private val json = Json { prettyPrint = true }

        fun loadFromFile(path: Path) {
            // require(!isLoaded) { "Config already loaded" }

            instance = if (FileSystem.SYSTEM.exists(path)) {
                FileSystem.SYSTEM.read(path) {
                    json.decodeFromString<DepotConfigStore>(readUtf8())
                }
            } else {
                DepotConfigStore()
            }

            filePath = path
        }

        fun save() {
            val currentInstance = requireNotNull(instance) { "Saved config before loading" }
            val currentPath = requireNotNull(filePath) { "File path not set" }

            currentPath.parent?.let { FileSystem.SYSTEM.createDirectories(it) }

            FileSystem.SYSTEM.write(currentPath) {
                writeUtf8(json.encodeToString(currentInstance))
            }
        }

        @Throws(IllegalArgumentException::class)
        fun getInstance(): DepotConfigStore = requireNotNull(instance) { "Config not loaded" }
    }
}
