package `in`.dragonbra.javasteam.depotdownloader

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

/**
 * Singleton storage for tracking installed depot manifests.
 * Persists manifest IDs to disk to enable incremental updates and avoid
 * re-downloading unchanged content. The configuration is serialized as JSON
 * and must be loaded via [loadFromFile] before use.
 *
 * @property installedManifestIDs Map of depot IDs to their currently installed manifest IDs
 *
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
