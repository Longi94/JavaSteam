package `in`.dragonbra.javasteam.steam.steamclient

/**
 * Thrown when Steam encounters a remote error with a pending [in.dragonbra.javasteam.types.AsyncJob].
 * @author Lossy
 * @since 2023-03-17
 */
@Suppress("unused")
class AsyncJobFailedException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
}
