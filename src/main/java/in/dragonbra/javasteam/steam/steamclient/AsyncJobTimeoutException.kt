package `in`.dragonbra.javasteam.steam.steamclient

import `in`.dragonbra.javasteam.util.JavaSteamAddition
import java.util.concurrent.CancellationException

/**
 * Thrown when a pending [in.dragonbra.javasteam.types.AsyncJob] gets no answer from Steam in time.
 *
 * As in SteamKit a timed out job is cancelled, so this is a [CancellationException] and the job's future
 * reports `isCancelled`. Catching this type tells a timeout apart from the caller itself being cancelled.
 */
@Suppress("unused")
@JavaSteamAddition
class AsyncJobTimeoutException : CancellationException {
    constructor() : super("The async job timed out waiting for a response from Steam.")
    constructor(message: String?) : super(message)
}
