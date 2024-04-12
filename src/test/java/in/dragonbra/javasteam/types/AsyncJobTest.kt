package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

// Note: await() is kotlin only.
// Note: Deferred is an alternative to C#'s Task<>; however, when a deferred is completed exceptionally,
// it's not considered cancelled. Also, there is no "isFaulted" state too.
class AsyncJobTest {

    internal class Callback : CallbackMsg() {
        var isFinished: Boolean? = null
    }

    @Test
    fun asyncJobCtorRegistersJob() {
        val client = SteamClient()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        Assertions.assertTrue(
            client.jobManager.asyncJobs.containsKey(asyncJob.jobID),
            "Async job dictionary should contain the jobid key"
        )

        Assertions.assertTrue(
            client.jobManager.asyncJobs.containsKey(JobID(123)),
            "Async job dictionary should contain jobid key as a value type"
        )
    }

    @Test
    fun asyncJobCompletesOnCallback() {
        val client = SteamClient()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val asyncTask = asyncJob.toDeferred()

        val callback = Callback()
        callback.jobID = JobID(123)
        client.postCallback(callback)

        Assertions.assertTrue(asyncTask.isCompleted, "Async job should be completed after callback is posted")
        Assertions.assertFalse(asyncTask.isCancelled, "Async job should not be canceled after callback is posted")
    }

    @Test
    fun asyncJobClearsOnCompletion() {
        val client = SteamClient()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val callback = Callback()
        callback.jobID = JobID(123)
        client.postCallback(callback)

        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(asyncJob.jobID),
            "Async job dictionary should no longer contain jobid key after callback is posted"
        )
        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(JobID(123)),
            "Async job dictionary should no longer contain jobid key (as value type) after callback is posted"
        )
    }

    @Test
    fun asyncJobClearsOnTimeout() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        asyncJob.timeout = 1000

        Thread.sleep(5000)

        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(asyncJob.jobID),
            "Async job dictionary should no longer contain jobid key after timeout"
        )
        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(JobID(123)),
            "Async job dictionary should no longer contain jobid key (as value type) after timeout"
        )
    }

    @Test
    fun asyncJobCancelsOnSetFailedTimeout() {
        val client = SteamClient()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val asyncTask = asyncJob.toDeferred()
        asyncJob.setFailed(false)

        Assertions.assertTrue(asyncTask.isCompleted, "Async job should be completed on message timeout")
        Assertions.assertTrue(asyncTask.isCancelled, "Async job should be canceled on message timeout")
        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobGivesBackCallback() {
        val client = SteamClient()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val asyncTask = asyncJob.toDeferred()
        val ourCallback = Callback().apply {
            jobID = JobID(123)
        }

        client.postCallback(ourCallback)

        runBlocking {
            Assertions.assertSame(asyncTask.await(), ourCallback)
        }
    }

    @Test
    fun asyncJobTimesOut() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123)).apply {
            timeout = 1000
        }
        val asyncTask = asyncJob.toDeferred()

        Thread.sleep(5000L)

        Assertions.assertTrue(
            asyncTask.isCompleted,
            "Async job should be completed after 5 seconds of a 1 second job timeout"
        )
        Assertions.assertTrue(
            asyncTask.isCancelled,
            "Async job should be canceled after 5 seconds of a 1 second job timeout"
        )
        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobThrowsExceptionOnNullCallback() {
        val client = SteamClient()

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        Assertions.assertThrows(IllegalArgumentException::class.java) { asyncJob.addResult(null) }
    }

    @Test
    fun asyncJobThrowsFailureExceptionOnFailure() {
        val client = SteamClient()

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val asyncTask = asyncJob.toDeferred()

        asyncJob.setFailed(true)

        Assertions.assertTrue(asyncTask.isCompleted, "Async job should be completed after job failure")
        Assertions.assertThrows(AsyncJobFailedException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobMultipleFinishedOnEmptyPredicate() {
        val client = SteamClient()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }
        val asyncTask = asyncJob.toDeferred()

        val jobFinished = asyncJob.addResult(Callback().apply { jobID = JobID(123) })

        Assertions.assertTrue(
            jobFinished,
            "Async job should inform that it is completed when completion predicate is always true and a result is given"
        )
        Assertions.assertTrue(
            asyncTask.isCompleted,
            "Async job should be completed when empty predicate result is given"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when empty predicate result is given"
        )
    }

    @Test
    fun asyncJobMultipleFinishedOnPredicate() {
        val client = SteamClient()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }
        val asyncTask = asyncJob.toDeferred()

        var jobFinished = asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = false
            }
        )

        Assertions.assertFalse(
            jobFinished,
            "Async job should not inform that it is finished when completion predicate is false after a result is given"
        )
        Assertions.assertFalse(
            asyncTask.isCompleted,
            "Async job should not be completed when completion predicate is false"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when completion predicate is false"
        )

        jobFinished = asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = true
            }
        )

        Assertions.assertTrue(
            jobFinished,
            "Async job should inform completion when completion predicate is passed after a result is given"
        )
        Assertions.assertTrue(
            asyncTask.isCompleted,
            "Async job should be completed when completion predicate is true"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when completion predicate is true"
        )
    }

    @Test
    fun asyncJobMultipleClearsOnCompletion() {
        val client = SteamClient()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }

        client.postCallback(
            Callback().apply {
                jobID = JobID(123)
                isFinished = true
            }
        )

        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(asyncJob.jobID),
            "Async job dictionary should not contain jobid key for AsyncJobMultiple on completion"
        )
        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(JobID(123)),
            "Async job dictionary should not contain jobid key (as value type) for AsyncJobMultiple on completion"
        )
    }

    @Test
    fun asyncJobMultipleClearsOnTimeout() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }
        asyncJob.timeout = 1000

        runBlocking { delay(5000) }

        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(asyncJob.jobID),
            "Async job dictionary should no longer contain jobid key after timeout"
        )
        Assertions.assertFalse(
            client.jobManager.asyncJobs.containsKey(JobID(123)),
            "Async job dictionary should no longer contain jobid key (as value type) after timeout"
        )
    }

    @Test
    fun asyncJobMultipleExtendsTimeoutOnMessage() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }
        asyncJob.timeout = 5000

        val asyncTask = asyncJob.toDeferred()

        // wait 3 seconds before we post any results to this job at all
        runBlocking { delay(3000) }

        Assertions.assertFalse(
            asyncTask.isCompleted,
            "AsyncJobMultiple should not be completed after 3 seconds of 5 second timeout"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled after 3 seconds of 5 second timeout"
        )

        // give result 1 of 2
        asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = false
            }
        )

        // delay for what the original timeout would have been
        runBlocking { delay(5000) }

        // we still shouldn't be completed or canceled (timed out)
        Assertions.assertFalse(
            asyncTask.isCompleted,
            "AsyncJobMultiple should not be completed 5 seconds after a result was added (result should extend timeout)"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled 5 seconds after a result was added (result should extend timeout)"
        )

        asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = true
            }
        )

        // we should be completed but not canceled or faulted
        Assertions.assertTrue(
            asyncTask.isCompleted,
            "AsyncJobMultiple should be completed when final result is added to set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled when final result is added to set"
        )
    }

    @Test
    fun asyncJobMultipleTimesOut() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toDeferred()

        runBlocking {
            delay(5000)
        }

        Assertions.assertTrue(
            asyncTask.isCompleted,
            "AsyncJobMultiple should be completed after 5 seconds of a 1 second job timeout"
        )
        Assertions.assertTrue(
            asyncTask.isCancelled,
            "AsyncJobMultiple should be canceled after 5 seconds of a 1 second job timeout"
        )

        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun asyncJobMultipleCompletesOnIncompleteResult() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toDeferred()

        val onlyResult = Callback().apply {
            jobID = JobID(123)
            isFinished = false
        }

        asyncJob.addResult(onlyResult)

        // adding a result will extend the job's timeout, but we'll cheat here and decrease it
        asyncJob.timeout = 1000

        runBlocking {
            delay(5000)
        }

        Assertions.assertTrue(
            asyncTask.isCompleted,
            "AsyncJobMultiple should be completed on partial (timed out) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled on partial (timed out) result set"
        )

        val result: AsyncJobMultiple.ResultSet = asyncTask.getCompleted()

        Assertions.assertFalse(result.complete, "ResultSet should be incomplete")
        Assertions.assertFalse(result.failed, "ResultSet should not be failed")
        Assertions.assertTrue(result.results.size == 1)
        Assertions.assertSame(onlyResult, result.results.first())
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun asyncJobMultipleCompletesOnIncompleteResultAndFailure() {
        val client = SteamClient().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toDeferred()

        val onlyResult = Callback().apply {
            jobID = JobID(123)
            isFinished = false
        }

        asyncJob.addResult(onlyResult)
        asyncJob.setFailed(true)

        Assertions.assertTrue(
            asyncTask.isCompleted,
            "AsyncJobMultiple should be completed on partial (failed) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled on partial (failed) result set"
        )

        val result: AsyncJobMultiple.ResultSet = asyncTask.getCompleted()

        Assertions.assertFalse(result.complete, "ResultSet should be incomplete")
        Assertions.assertTrue(result.failed, "ResultSet should be failed")
        Assertions.assertTrue(result.results.size == 1)
        Assertions.assertSame(onlyResult, result.results.first())
    }

    @Test
    fun asyncJobMultipleThrowsExceptionOnNullCallback() {
        val client = SteamClient()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }

        Assertions.assertThrows(IllegalArgumentException::class.java) { asyncJob.addResult(null) }
    }

    @Test
    fun asyncJobMultipleThrowsFailureExceptionOnFailure() {
        val client = SteamClient()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }
        val asyncTask = asyncJob.toDeferred()

        asyncJob.setFailed(true)

        Assertions.assertTrue(asyncTask.isCompleted, "AsyncJobMultiple should be completed after job failure")
        Assertions.assertThrows(AsyncJobFailedException::class.java) { runBlocking { asyncTask.await() } }
    }

//    @Test
//    fun asyncJobContinuesAsynchronously()

//    @Test
//    fun AsyncJobMultipleContinuesAsynchronously()
}
