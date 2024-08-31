package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

/**
 * @author lngtr
 * @since 2018-02-23
 */
interface ICallbackMgrInternals {
    fun register(callback: CallbackBase)

    fun unregister(callback: CallbackBase)
}
