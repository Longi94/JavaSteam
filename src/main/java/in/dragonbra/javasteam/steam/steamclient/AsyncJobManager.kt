package `in`.dragonbra.javasteam.steam.steamclient

import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJob
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.event.ScheduledFunction
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class AsyncJobManager {

    val asyncJobs: ConcurrentMap<JobID, AsyncJob> = ConcurrentHashMap()

    private val jobTimeoutFunc: ScheduledFunction = ScheduledFunction(this::cancelTimedOutJobs, 1000)

    /**
     * Tracks a job with this manager.
     *
     * @param asyncJob The asynchronous job to track
     */
    fun startJob(asyncJob: AsyncJob) {
        asyncJobs[asyncJob.jobID] = asyncJob
    }

    /**
     * Passes a callback to a pending async job.
     * If the given callback completes the job, the job is removed from this manager.
     *
     * @param jobID    the job.
     * @param callback the callback.
     */
    fun tryCompleteJob(jobID: JobID, callback: CallbackMsg?) {
        // if not a job we are tracking ourselves, can ignore it
        val asyncJob = getJob(jobID) ?: return

        // pass this callback into the job,
        // so it can determine if the job is finished (in the case of multiple responses to a job)
        val jobFinished: Boolean = asyncJob.addResult(callback)

        if (jobFinished) {
            // if the job is finished, we can stop tracking it
            asyncJobs.remove(jobID)
        }
    }

    /**
     * Extends the lifetime of a job.
     *
     * @param jobID The job identifier.
     */
    fun heartbeatJob(jobID: JobID) {
        // ignore heartbeats for jobs we're not tracking
        val asyncJob: AsyncJob = getJob(jobID) ?: return

        asyncJob.heartbeat()
    }

    /**
     * Marks a certain job as remotely failed.
     *
     * @param jobID The job identifier.
     */
    fun failJob(jobID: JobID) {
        // ignore remote failures for jobs we're not tracking
        val asyncJob: AsyncJob = getJob(jobID, true) ?: return

        asyncJob.setFailed(true)
    }

    /**
     * Cancels and clears all jobs being tracked.
     */
    fun cancelPendingJobs() {
        asyncJobs.values.forEach { job ->
            job.setFailed(false)
        }

        asyncJobs.clear()
    }

    /**
     * Enables or disables periodic checks for job timeouts.
     *
     * @param enable Whether the job timeout checks should be enabled.
     */
    fun setTimeoutsEnabled(enable: Boolean) {
        if (enable) {
            jobTimeoutFunc.start()
        } else {
            jobTimeoutFunc.stop()
        }
    }

    /**
     * This is called periodically to cancel and clear out any jobs that have timed out (no response from Steam).
     */
    private fun cancelTimedOutJobs() {
        asyncJobs.values.forEach { job ->
            if (job.isTimedOut) {
                job.setFailed(false)
                asyncJobs.remove(job.jobID)
            }
        }
    }

    /**
     * Retrieves a job from this manager, and optionally removes it from tracking.
     *
     * @param jobID     the job id.
     * @param andRemove If set to <c>true</c>, this job is removed from tracking.
     * @return .
     */
    private fun getJob(jobID: JobID, andRemove: Boolean = false): AsyncJob? {
        val asyncJob: AsyncJob?
        val foundJob: Boolean

        if (andRemove) {
            asyncJob = asyncJobs[jobID]
            foundJob = asyncJobs.remove(jobID, asyncJobs[jobID])
        } else {
            asyncJob = asyncJobs[jobID]
            foundJob = asyncJob != null
        }

        return if (foundJob) asyncJob else null
    }
}