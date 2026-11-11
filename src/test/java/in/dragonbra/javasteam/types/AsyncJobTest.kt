package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.ConnectedSteamClient
import `in`.dragonbra.javasteam.steam.steamclient.AsyncJobFailedException
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds

class AsyncJobTest {

    internal class Callback : CallbackMsg() {
        var isFinished: Boolean? = null
    }

    @Test
    fun asyncJobFailureWhenClientDiconnected() {
        val client = SteamClient()

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val jobTask = asyncJob.toFuture()

        Assertions.assertTrue(jobTask.isDone, "Async job should be completed when client is disconnected")
        Assertions.assertFalse(jobTask.isCancelled, "Async job should not be when client is disconnected")
        Assertions.assertTrue(jobTask.isCompletedExceptionally, "Async job should not be when client is disconnected")
    }

    @Test
    fun asyncJobCtorRegistersJob() {
        val client = ConnectedSteamClient.get()
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
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val asyncTask = asyncJob.toFuture()

        val callback = Callback()
        callback.jobID = JobID(123)
        client.postCallback(callback)

        Assertions.assertTrue(asyncTask.isDone, "Async job should be completed after callback is posted")
        Assertions.assertFalse(asyncTask.isCancelled, "Async job should not be canceled after callback is posted")
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "Async job should be faulted when client is disconnected"
        )
    }

    @Test
    fun asyncJobClearsOnCompletion() {
        val client = ConnectedSteamClient.get()
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
        val client = ConnectedSteamClient.get().apply {
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
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val asyncTask = asyncJob.toFuture()
        asyncJob.setFailed(false)

        Assertions.assertTrue(asyncTask.isDone, "Async job should be completed on message timeout")
        Assertions.assertTrue(asyncTask.isCancelled, "Async job should be canceled on message timeout")
        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobGivesBackCallback() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val asyncTask = asyncJob.toFuture()
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
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123)).apply {
            timeout = 1000
        }
        val asyncTask = asyncJob.toFuture()

        Thread.sleep(5000L)

        Assertions.assertTrue(
            asyncTask.isDone,
            "Async job should be completed after 5 seconds of a 1 second job timeout"
        )
        Assertions.assertTrue(
            asyncTask.isCancelled,
            "Async job should be canceled after 5 seconds of a 1 second job timeout"
        )
        // Assertions.assertFalse(
        //     asyncTask.isCompletedExceptionally,
        //     "Async job should not be faulted yet"
        // )
        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobThrowsFailureExceptionOnFailure() {
        val client = ConnectedSteamClient.get()

        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val asyncTask = asyncJob.toFuture()

        asyncJob.setFailed(true)

        Assertions.assertTrue(asyncTask.isDone, "Async job should be completed after job failure")
        Assertions.assertFalse(asyncTask.isCancelled, "Async job should not be canceled after job failure")
        Assertions.assertTrue(asyncTask.isCompletedExceptionally, "Async job should be faulted after job failure")
        Assertions.assertThrows(AsyncJobFailedException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobMultipleFinishedOnEmptyPredicate() {
        val client = ConnectedSteamClient.get()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }
        val asyncTask = asyncJob.toFuture()

        val jobFinished = asyncJob.addResult(Callback().apply { jobID = JobID(123) })

        Assertions.assertTrue(
            jobFinished,
            "Async job should inform that it is completed when completion predicate is always true and a result is given"
        )
        Assertions.assertTrue(
            asyncTask.isDone,
            "Async job should be completed when empty predicate result is given"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when empty predicate result is given"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "Async job should not be faulted when empty predicate result is given"
        )
    }

    @Test
    fun asyncJobMultipleFinishedOnPredicate() {
        val client = ConnectedSteamClient.get()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }
        val asyncTask = asyncJob.toFuture()

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
            asyncTask.isDone,
            "Async job should not be completed when completion predicate is false"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when completion predicate is false"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "Async job should not be faulted when completion predicate is false"
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
            asyncTask.isDone,
            "Async job should be completed when completion predicate is true"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "Async job should not be canceled when completion predicate is true"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "Async job should not be faulted when completion predicate is true"
        )
    }

    @Test
    fun asyncJobMultipleClearsOnCompletion() {
        val client = ConnectedSteamClient.get()

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
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }
        asyncJob.timeout = 1000

        runBlocking { delay(5.seconds) }

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
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }
        asyncJob.timeout = 5000

        val asyncTask = asyncJob.toFuture()

        // wait 3 seconds before we post any results to this job at all
        runBlocking { delay(3.seconds) }

        Assertions.assertFalse(
            asyncTask.isDone,
            "AsyncJobMultiple should not be completed after 3 seconds of 5 second timeout"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled after 3 seconds of 5 second timeout"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should not be faulted yet"
        )

        // give result 1 of 2
        asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = false
            }
        )

        // delay for what the original timeout would have been
        runBlocking { delay(5.seconds) }

        // we still shouldn't be completed or canceled (timed out)
        Assertions.assertFalse(
            asyncTask.isDone,
            "AsyncJobMultiple should not be completed 5 seconds after a result was added (result should extend timeout)"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled 5 seconds after a result was added (result should extend timeout)"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should not be faulted yet after a result was added (result should extend timeout)"
        )

        asyncJob.addResult(
            Callback().apply {
                jobID = JobID(123)
                isFinished = true
            }
        )

        // we should be completed but not canceled or faulted
        Assertions.assertTrue(
            asyncTask.isDone,
            "AsyncJobMultiple should be completed when final result is added to set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled when final result is added to set"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should not be faulted when final result is added to set"
        )
    }

    @Test
    fun asyncJobMultipleTimesOut() {
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toFuture()

        runBlocking {
            delay(5.seconds)
        }

        Assertions.assertTrue(
            asyncTask.isDone,
            "AsyncJobMultiple should be completed after 5 seconds of a 1 second job timeout"
        )
        Assertions.assertTrue(
            asyncTask.isCancelled,
            "AsyncJobMultiple should be canceled after 5 seconds of a 1 second job timeout"
        )
        // Assertions.assertFalse(
        //     asyncTask.isCompletedExceptionally,
        //     "AsyncJobMultiple should not be faulted after job timeout"
        // )

        Assertions.assertThrows(CancellationException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobMultipleCompletesOnIncompleteResult() {
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toFuture()

        val onlyResult = Callback().apply {
            jobID = JobID(123)
            isFinished = false
        }

        asyncJob.addResult(onlyResult)

        // adding a result will extend the job's timeout, but we'll cheat here and decrease it
        asyncJob.timeout = 1000

        runBlocking {
            delay(5.seconds)
        }

        Assertions.assertTrue(
            asyncTask.isDone,
            "AsyncJobMultiple should be completed on partial (timed out) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled on partial (timed out) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should not be faulted on a partial (failed) result set"
        )

        val result = asyncTask.get()

        Assertions.assertFalse(result.complete, "ResultSet should be incomplete")
        Assertions.assertFalse(result.failed, "ResultSet should not be failed")
        Assertions.assertTrue(result.results.size == 1)
        Assertions.assertSame(onlyResult, result.results.first())
    }

    @Test
    fun asyncJobMultipleCompletesOnIncompleteResultAndFailure() {
        val client = ConnectedSteamClient.get().apply {
            jobManager.setTimeoutsEnabled(true)
        }

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }.apply {
            timeout = 1000
        }

        val asyncTask = asyncJob.toFuture()

        val onlyResult = Callback().apply {
            jobID = JobID(123)
            isFinished = false
        }

        asyncJob.addResult(onlyResult)
        asyncJob.setFailed(true)

        Assertions.assertTrue(
            asyncTask.isDone,
            "AsyncJobMultiple should be completed on partial (failed) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCancelled,
            "AsyncJobMultiple should not be canceled on partial (failed) result set"
        )
        Assertions.assertFalse(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should not be faulted on a partial (failed) result set"
        )

        val result = asyncTask.get()

        Assertions.assertFalse(result.complete, "ResultSet should be incomplete")
        Assertions.assertTrue(result.failed, "ResultSet should be failed")
        Assertions.assertTrue(result.results.size == 1)
        Assertions.assertSame(onlyResult, result.results.first())
    }

    @Test
    fun asyncJobMultipleThrowsFailureExceptionOnFailure() {
        val client = ConnectedSteamClient.get()

        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }
        val asyncTask = asyncJob.toFuture()

        asyncJob.setFailed(true)

        Assertions.assertTrue(asyncTask.isDone, "AsyncJobMultiple should be completed after job failure")
        Assertions.assertFalse(asyncTask.isCancelled, "AsyncJobMultiple should not be canceled after job failure")
        Assertions.assertTrue(
            asyncTask.isCompletedExceptionally,
            "AsyncJobMultiple should be faulted after job failure"
        )
        Assertions.assertThrows(AsyncJobFailedException::class.java) { runBlocking { asyncTask.await() } }
    }

    @Test
    fun asyncJobContinuesAsynchronously() = runBlocking {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        var continuationThread: Thread? = null

        // UNDISPATCHED runs up to await() on this thread, so the continuation is registered before we complete
        val continuation = launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
            asyncJob.await()
            continuationThread = Thread.currentThread()
        }

        val completionThread = Thread.currentThread()
        asyncJob.addResult(Callback().apply { jobID = JobID(123) })

        continuation.join()

        Assertions.assertNotNull(continuationThread, "Continuation should have run")
        Assertions.assertNotSame(
            completionThread,
            continuationThread,
            "Continuation should not run on the thread that completed the job"
        )
    }

    @Test
    fun asyncJobMultipleContinuesAsynchronously() = runBlocking {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }

        var continuationThread: Thread? = null

        // UNDISPATCHED runs up to await() on this thread, so the continuation is registered before we complete
        val continuation = launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
            asyncJob.await()
            continuationThread = Thread.currentThread()
        }

        val completionThread = Thread.currentThread()
        asyncJob.addResult(Callback().apply { jobID = JobID(123) })

        continuation.join()

        Assertions.assertNotNull(continuationThread, "Continuation should have run")
        Assertions.assertNotSame(
            completionThread,
            continuationThread,
            "Continuation should not run on the thread that completed the job"
        )
    }

    @Test
    fun asyncJobToFutureReturnsSameInstance() {
        val client = ConnectedSteamClient.get()

        val single = AsyncJobSingle<Callback>(client, JobID(123))
        val multiple = AsyncJobMultiple<Callback>(client, JobID(124)) { _ -> true }

        Assertions.assertSame(single.toFuture(), single.toFuture())
        Assertions.assertSame(multiple.toFuture(), multiple.toFuture())
    }

    @Test
    fun asyncJobHeartbeatExtendsTimeout() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        Assertions.assertEquals(10_000L, asyncJob.timeout)
        Assertions.assertFalse(asyncJob.isTimedOut)

        asyncJob.heartbeat()
        Assertions.assertEquals(20_000L, asyncJob.timeout)

        asyncJob.timeout = 0
        Assertions.assertTrue(asyncJob.isTimedOut)
    }

    //region Blocking and CompletableFuture paths, as a Java caller would use them (no coroutines)

    @Test
    fun asyncJobRunBlockReturnsCallbackPostedFromAnotherThread() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val ourCallback = Callback().apply { jobID = JobID(123) }

        val poster = thread(name = "job-poster") {
            Thread.sleep(100)
            client.postCallback(ourCallback)
        }

        val result = Assertions.assertTimeoutPreemptively(TEST_TIMEOUT, ThrowingSupplier { asyncJob.runBlock() })
        poster.join()

        Assertions.assertSame(ourCallback, result)
    }

    @Test
    fun asyncJobMultipleRunBlockReturnsResultSetPostedFromAnotherThread() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { call -> call.isFinished }

        val first = Callback().apply {
            jobID = JobID(123)
            isFinished = false
        }
        val second = Callback().apply {
            jobID = JobID(123)
            isFinished = true
        }

        val poster = thread(name = "job-poster") {
            Thread.sleep(100)
            client.postCallback(first)
            client.postCallback(second)
        }

        val result = Assertions.assertTimeoutPreemptively(TEST_TIMEOUT, ThrowingSupplier { asyncJob.runBlock() })
        poster.join()

        Assertions.assertTrue(result.complete, "ResultSet should be complete")
        Assertions.assertFalse(result.failed, "ResultSet should not be failed")
        Assertions.assertEquals(listOf(first, second), result.results)
    }

    @Test
    fun asyncJobRunBlockThrowsExecutionExceptionOnFailure() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        asyncJob.setFailed(true)

        val exception = Assertions.assertThrows(ExecutionException::class.java) { asyncJob.runBlock() }
        Assertions.assertInstanceOf(AsyncJobFailedException::class.java, exception.cause)
    }

    @Test
    fun asyncJobRunBlockThrowsCancellationExceptionOnTimeout() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        asyncJob.setFailed(false)

        Assertions.assertThrows(CancellationException::class.java) { asyncJob.runBlock() }
    }

    @Test
    fun asyncJobMultipleRunBlockThrowsExecutionExceptionOnFailure() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }

        asyncJob.setFailed(true)

        val exception = Assertions.assertThrows(ExecutionException::class.java) { asyncJob.runBlock() }
        Assertions.assertInstanceOf(AsyncJobFailedException::class.java, exception.cause)
    }

    @Test
    fun asyncJobMultipleRunBlockThrowsCancellationExceptionOnTimeout() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> false }

        asyncJob.setFailed(false)

        Assertions.assertThrows(CancellationException::class.java) { asyncJob.runBlock() }
    }

    @Test
    fun asyncJobFutureGetTimesOutWhilePending() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        Assertions.assertThrows(TimeoutException::class.java) {
            asyncJob.toFuture().get(50, TimeUnit.MILLISECONDS)
        }
        Assertions.assertFalse(asyncJob.toFuture().isDone, "A get() timeout should not complete the job")
    }

    @Test
    fun asyncJobFutureContinuationRunsOnCompletingThread() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))

        val continuationThread = AtomicReference<Thread>()
        asyncJob.toFuture().thenAccept { continuationThread.set(Thread.currentThread()) }

        asyncJob.addResult(Callback().apply { jobID = JobID(123) })

        // Documented behavior: a non-async continuation runs inline, before addResult returns
        Assertions.assertSame(Thread.currentThread(), continuationThread.get())
    }

    @Test
    fun asyncJobFutureAsyncContinuationRunsOffCompletingThread() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobSingle<Callback>(client, JobID(123))
        val executor = Executors.newSingleThreadExecutor()

        try {
            val continuationThread = AtomicReference<Thread>()
            val continuation = asyncJob.toFuture().thenAcceptAsync(
                { continuationThread.set(Thread.currentThread()) },
                executor
            )

            asyncJob.addResult(Callback().apply { jobID = JobID(123) })
            continuation.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)

            Assertions.assertNotNull(continuationThread.get(), "Continuation should have run")
            Assertions.assertNotSame(Thread.currentThread(), continuationThread.get())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun asyncJobMultipleFutureAsyncContinuationRunsOffCompletingThread() {
        val client = ConnectedSteamClient.get()
        val asyncJob = AsyncJobMultiple<Callback>(client, JobID(123)) { _ -> true }
        val executor = Executors.newSingleThreadExecutor()

        try {
            val continuationThread = AtomicReference<Thread>()
            val continuation = asyncJob.toFuture().thenAcceptAsync(
                { continuationThread.set(Thread.currentThread()) },
                executor
            )

            asyncJob.addResult(Callback().apply { jobID = JobID(123) })
            continuation.get(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)

            Assertions.assertNotNull(continuationThread.get(), "Continuation should have run")
            Assertions.assertNotSame(Thread.currentThread(), continuationThread.get())
        } finally {
            executor.shutdownNow()
        }
    }

    //endregion

    private companion object {
        val TEST_TIMEOUT: java.time.Duration = java.time.Duration.ofSeconds(5)
    }
}
