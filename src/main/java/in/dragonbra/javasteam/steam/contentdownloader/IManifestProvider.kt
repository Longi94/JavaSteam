package `in`.dragonbra.javasteam.steam.contentdownloader

import `in`.dragonbra.javasteam.types.DepotManifest

/**
 * An interface for persisting depot manifests for Steam content downloading
 */
interface IManifestProvider {

    /**
     * Ask a provider to fetch a specific depot manifest
     * @return A [Pair] object with a [DepotManifest] and its checksum if it exists otherwise null
     */
    fun fetchManifest(depotID: Int, manifestID: Long): DepotManifest?

    /**
     * Ask a provider to fetch the most recent manifest used of a depot
     * @return A [Pair] object with a [DepotManifest] and its checksum if it exists otherwise null
     */
    fun fetchLatestManifest(depotID: Int): DepotManifest?

    /**
     * Ask a provider to set the most recent manifest ID used of a depot
     */
    fun setLatestManifestId(depotID: Int, manifestID: Long)

    /**
     * Update the persistent depot manifest
     * @param manifest The depot manifest
     * @return The checksum of the depot manifest
     */
    fun updateManifest(manifest: DepotManifest)
}
