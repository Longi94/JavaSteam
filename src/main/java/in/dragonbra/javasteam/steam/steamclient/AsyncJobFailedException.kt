package `in`.dragonbra.javasteam.steam.steamclient

/**
 * Thrown when Steam encounters a remote error with a pending <see cref="AsyncJob"></see>.
 */
class AsyncJobFailedException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
}