package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest

class MemoryManifestProvider : IManifestProvider {

    private val depotManifests = mutableMapOf<Int, MutableMap<Long, DepotManifest>>()
    private val latestManifests = mutableMapOf<Int, Long>()

    override fun fetchManifest(depotID: Int, manifestID: Long): Pair<DepotManifest, ByteArray>? {
        return depotManifests[depotID]?.get(manifestID)?.let { Pair(it, it.calculateChecksum()) }
    }

    override fun fetchLatestManifest(depotID: Int): Pair<DepotManifest, ByteArray>? {
        return latestManifests[depotID]?.let { fetchManifest(depotID, it) }
    }

    override fun setLatestManifestId(depotID: Int, manifestID: Long) {
        latestManifests[depotID] = manifestID
    }

    override fun updateManifest(manifest: DepotManifest): ByteArray {
        depotManifests.getOrPut(manifest.depotID) { mutableMapOf() }[manifest.manifestGID] = manifest
        return manifest.calculateChecksum()
    }
}
