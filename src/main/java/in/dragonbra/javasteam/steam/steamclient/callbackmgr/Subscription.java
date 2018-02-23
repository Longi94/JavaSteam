package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author lngtr
 * @since 2018-02-23
 */
public class Subscription implements Closeable {

    private ICallbackMgrInternals manager;
    private CallbackBase call;

    public Subscription(ICallbackMgrInternals manager, CallbackBase call) {
        this.manager = manager;
        this.call = call;
    }

    @Override
    public void close() throws IOException {
        if (call != null && manager != null) {
            manager.unregister(call);
            call = null;
            manager = null;
        }
    }
}
