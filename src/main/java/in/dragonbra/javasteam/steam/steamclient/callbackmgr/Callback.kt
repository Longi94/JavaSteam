package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import java.io.Closeable

@Suppress("unused")
class Callback<TCall : CallbackMsg> @JvmOverloads constructor(
    override val callbackType: Class<out TCall>,
    val onRun: suspend (TCall) -> Unit,
    private var mgr: CallbackManager? = null,
    val jobID: JobID = JobID.INVALID,
) : CallbackBase(),
    Closeable {

    init {
        mgr?.register(this)
    }

    override fun close() {
        mgr?.unregister(this)
        mgr = null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun run(callback: Any) {
        val cb = callback as? TCall ?: return
        if (cb.jobID == jobID || jobID == JobID.INVALID) {
            onRun(cb)
        }
    }
}
