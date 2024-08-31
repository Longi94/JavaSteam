package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import java.io.Closeable

/**
 * @author lngtr
 * @since 2018-02-23
 */
class Subscription(
    private var manager: ICallbackMgrInternals?,
    private var call: CallbackBase?,
) : Closeable {

    override fun close() {
        call?.let { manager?.unregister(it) }
        call = null
        manager = null
    }
}
