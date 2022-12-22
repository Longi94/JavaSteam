package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a utility for routing callbacks to function calls.
 * In order to bind callbacks to functions, an instance of this class must be created for the
 * {@link in.dragonbra.javasteam.steam.steamclient.SteamClient SteamClient} instance that will be posting callbacks.
 */
public class CallbackManager implements ICallbackMgrInternals {

    private SteamClient steamClient;

    private Set<CallbackBase> registeredCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Initializes a new instance of the {@link CallbackManager} class.
     *
     * @param steamClient The {@link in.dragonbra.javasteam.steam.steamclient.SteamClient SteamClient} instance to handle the callbacks of.
     */
    public CallbackManager(SteamClient steamClient) {
        if (steamClient == null) {
            throw new IllegalArgumentException("steamclient is null");
        }
        this.steamClient = steamClient;
    }

    /**
     * Runs a single queued callback.
     * If no callback is queued, this method will instantly return.
     */
    public void runCallbacks() {
        ICallbackMsg call = steamClient.getCallback(true);

        if (call != null) {
            handle(call);
        }
    }

    /**
     * Blocks the current thread to run a single queued callback.
     * If no callback is queued, the method will block for the given timeout.
     *
     * @param timeout The length of time to block.
     */
    public void runWaitCallbacks(long timeout) {
        ICallbackMsg call = steamClient.waitForCallback(true, timeout);

        if (call != null) {
            handle(call);
        }
    }

    /**
     * Blocks the current thread to run a single queued callback.
     * If no callback is queued, the method will block until one is posted.
     */
    public void runWaitCallbacks() {
        ICallbackMsg call = steamClient.waitForCallback(true);

        if (call != null) {
            handle(call);
        }
    }

    /**
     * Blocks the current thread to run all queued callbacks.
     * If no callback is queued, the method will block for the given timeout.
     *
     * @param timeout The length of time to block.
     */
    public void runWaitAllCallbacks(int timeout) {
        List<ICallbackMsg> calls = steamClient.getAllCallbacks(true, timeout);
        for (ICallbackMsg call : calls) {
            handle(call);
        }
    }

    /**
     * REgisters the provided {@link Consumer} to receive callbacks of type {@link TCallback}
     *
     * @param callbackType type of the callback
     * @param jobID        The {@link JobID}  of the callbacks that should be subscribed to.
     * @param callbackFunc The function to invoke with the callback.
     * @param <TCallback>  The type of callback to subscribe to.
     * @return An {@link Closeable}. Disposing of the return value will unsubscribe the callbackFunc .
     */
    public <TCallback extends ICallbackMsg> Closeable subscribe(Class<? extends TCallback> callbackType, JobID jobID, Consumer<TCallback> callbackFunc) {
        if (jobID == null) {
            throw new IllegalArgumentException("jobID is null");
        }

        if (callbackFunc == null) {
            throw new IllegalArgumentException("callbackFunc is null");
        }

        Callback<TCallback> callback = new Callback<>(callbackType, callbackFunc, this, jobID);
        return new Subscription(this, callback);
    }

    /**
     * REgisters the provided {@link Consumer} to receive callbacks of type {@link TCallback}
     *
     * @param callbackType type of the callback
     * @param callbackFunc The function to invoke with the callback.
     * @param <TCallback>  The type of callback to subscribe to.
     * @return An {@link Closeable}. Disposing of the return value will unsubscribe the callbackFunc .
     */
    public <TCallback extends ICallbackMsg> Closeable subscribe(Class<? extends TCallback> callbackType, Consumer<TCallback> callbackFunc) {
        return subscribe(callbackType, JobID.INVALID, callbackFunc);
    }

    @Override
    public void register(CallbackBase callback) {
        registeredCallbacks.add(callback);
    }

    @Override
    public void unregister(CallbackBase callback) {
        registeredCallbacks.remove(callback);
    }

    private void handle(ICallbackMsg call) {
        for (CallbackBase callback : registeredCallbacks) {
            if (callback.getCallbackType().isAssignableFrom(call.getClass())) {
                callback.run(call);
            }
        }
    }

}
