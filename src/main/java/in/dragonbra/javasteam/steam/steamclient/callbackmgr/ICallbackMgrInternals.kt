package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

/**
 * @author lngtr
 * @since 2018-02-23
 */
public interface ICallbackMgrInternals {
    void register(CallbackBase callback);

    void unregister(CallbackBase callback);
}
