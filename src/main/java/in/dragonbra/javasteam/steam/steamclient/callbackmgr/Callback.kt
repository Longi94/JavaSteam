package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.compat.Consumer
import java.io.Closeable

@Suppress("unused")
class Callback<TCall : CallbackMsg> @JvmOverloads constructor(
    override val callbackType: Class<out TCall>,
    private val onRun: Consumer<TCall>?,
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
    override fun run(callback: Any) {
        val cb = callback as TCall
        if ((cb.jobID == jobID || jobID == JobID.INVALID) && onRun != null) {
            onRun.accept(cb)
        }
    }
}
