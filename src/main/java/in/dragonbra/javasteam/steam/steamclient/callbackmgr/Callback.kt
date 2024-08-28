package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.io.Closeable;

@SuppressWarnings("unused")
public class Callback<TCall extends ICallbackMsg> extends CallbackBase implements Closeable {

    ICallbackMgrInternals mgr;

    private final JobID jobID;

    private final Consumer<TCall> onRun;

    private final Class<? extends TCall> callbackType;

    public Callback(Class<? extends TCall> callbackType, Consumer<TCall> func) {
        this(callbackType, func, null);
    }

    public Callback(Class<? extends TCall> callbackType, Consumer<TCall> func, ICallbackMgrInternals mgr) {
        this(callbackType, func, mgr, JobID.INVALID);
    }

    public Callback(Class<? extends TCall> callbackType, Consumer<TCall> func, ICallbackMgrInternals mgr, JobID jobID) {
        this.jobID = jobID;
        this.onRun = func;
        this.callbackType = callbackType;

        attachTo(mgr);
    }

    void attachTo(ICallbackMgrInternals mgr) {
        if (mgr == null) {
            return;
        }

        this.mgr = mgr;
        mgr.register(this);
    }

    @Override
    Class<?> getCallbackType() {
        return callbackType;
    }

    @SuppressWarnings("unchecked")
    @Override
    void run(Object callback) {
        if (callbackType.isAssignableFrom(callback.getClass())) {
            TCall cb = (TCall) callback;

            if ((cb.getJobID().equals(jobID) || jobID.equals(JobID.INVALID)) && onRun != null) {
                onRun.accept(cb);
            }
        }
    }

    @Override
    public void close() {
        if (mgr != null) {
            mgr.unregister(this);
        }
    }
}
