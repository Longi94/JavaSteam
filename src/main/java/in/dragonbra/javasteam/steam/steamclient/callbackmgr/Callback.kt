package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import java.io.Closeable

@Suppress("unused")
class Callback<TCall : ICallbackMsg> @JvmOverloads constructor(
    override val callbackType: Class<out TCall>,
    func: Consumer<TCall>?,
    private var mgr: ICallbackMgrInternals? = null,
    var jobID: JobID = JobID.INVALID,
) : CallbackBase(),
    Closeable {

    private val onRun = func

    init {
        attachTo(mgr)
    }

    private fun attachTo(mgr: ICallbackMgrInternals?) {
        this.mgr = mgr ?: return
        mgr.register(this)
    }

    override fun close() {
        mgr?.unregister(this)
    }

    @Suppress("UNCHECKED_CAST")
    override fun run(callback: Any) {
        if (callbackType.isAssignableFrom(callback.javaClass)) {
            val cb = callback as TCall
            if ((cb.jobID == jobID || jobID == JobID.INVALID) && onRun != null) {
                onRun.accept(cb)
            }
        }
    }
}
