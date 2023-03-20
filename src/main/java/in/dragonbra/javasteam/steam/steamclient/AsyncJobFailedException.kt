package `in`.dragonbra.javasteam.steam.steamclient

/**
 * Thrown when Steam encounters a remote error with a pending <see cref="AsyncJob"></see>.
 * @author Lossy
 * @since 2023-03-17
 */
class AsyncJobFailedException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
}