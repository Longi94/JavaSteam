package `in`.dragonbra.javasteam.steam.contentdownloader

/**
 * Interface for Java to implement for progress updates
 */
interface ProgressCallback {
    fun onProgress(progress: Float)
}
