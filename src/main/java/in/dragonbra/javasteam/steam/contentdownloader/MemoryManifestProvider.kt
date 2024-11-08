package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest

/**
 * @author Oxters
 * @since 2024-11-06
 */
class MemoryManifestProvider : IManifestProvider {

    private val depotManifests = mutableMapOf<Int, MutableMap<Long, DepotManifest>>()
    private val latestManifests = mutableMapOf<Int, Long>()

    override fun fetchManifest(depotID: Int, manifestID: Long): DepotManifest? =
        depotManifests[depotID]?.get(manifestID)

    override fun fetchLatestManifest(depotID: Int): DepotManifest? =
        latestManifests[depotID]?.let { fetchManifest(depotID, it) }

    override fun setLatestManifestId(depotID: Int, manifestID: Long) {
        latestManifests[depotID] = manifestID
    }

    override fun updateManifest(manifest: DepotManifest) {
        depotManifests.getOrPut(manifest.depotID) { mutableMapOf() }[manifest.manifestGID] = manifest
    }
}
