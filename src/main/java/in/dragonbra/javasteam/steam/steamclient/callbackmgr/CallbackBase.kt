package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

/**
 * This is the base class for the utility [Callback] class.
 * This is for internal use only, and shouldn't be used directly.
 */
abstract class CallbackBase {
    internal abstract val callbackType: Class<*>
    internal abstract suspend fun run(callback: Any)
}
