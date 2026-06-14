package `in`.dragonbra.javasteam.steam.steamclient.callbackmgr

import `in`.dragonbra.javasteam.TestBase
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.JobID
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

class CallbackManagerAsyncTest : TestBase() {

    private lateinit var client: SteamClient
    private lateinit var mgr: CallbackManager

    @BeforeEach
    fun setUp() {
        client = SteamClient()
        mgr = CallbackManager(client)
    }

    @Test
    fun postedCallbacksTriggerActionsAsync() = runTest {
        val callbacks = Array(10) {
            CallbackForTest(uniqueID = UUID.randomUUID())
        }

        val numCallbacksRun = AtomicInteger(0)

        mgr.subscribe<CallbackForTest> { cb ->
            val index = numCallbacksRun.get()
            Assertions.assertTrue(index < callbacks.size)
            Assertions.assertEquals(callbacks[index].uniqueID, cb.uniqueID)
            numCallbacksRun.incrementAndGet()
        }.use {
            callbacks.forEach { client.postCallback(it) }

            repeat(callbacks.size) { i ->
                mgr.runWaitCallbackAsync()
                Assertions.assertEquals(i + 1, numCallbacksRun.get())
            }

            // Callbacks should have been freed.
            mgr.runWaitAllCallbacks(0L)
            Assertions.assertEquals(10, numCallbacksRun.get())
        }
    }

    @Test
    fun correctlyAwaitsForAsyncCallbacks() = runTest {
        val callback = CallbackForTest(uniqueID = UUID.randomUUID())

        val numCallbacksRun = AtomicInteger(0)

        mgr.subscribe<CallbackForTest> { cb ->
            delay(100.milliseconds)
            Assertions.assertEquals(callback.uniqueID, cb.uniqueID)
            numCallbacksRun.incrementAndGet()
        }.use {
            repeat(10) { client.postCallback(callback) }

            repeat(10) { i ->
                mgr.runWaitCallbackAsync()
                Assertions.assertEquals(i + 1, numCallbacksRun.get())
            }

            mgr.runWaitAllCallbacks(0L)
            Assertions.assertEquals(10, numCallbacksRun.get())
        }
    }

    @Test
    fun asyncCallbackWithJobIDTriggersAction() = runTest {
        val jobID = JobID(123456)
        val callback = CallbackForTest(uniqueID = UUID.randomUUID()).apply {
            this.jobID = jobID
        }

        val didCall = AtomicBoolean(false)

        mgr.subscribe<CallbackForTest>(jobID = JobID(123456)) { cb ->
            delay(0.milliseconds) // yield
            Assertions.assertEquals(callback.uniqueID, cb.uniqueID)
            Assertions.assertEquals(jobID, cb.jobID)
            didCall.set(true)
        }.use {
            client.postCallback(callback)
            mgr.runWaitCallbackAsync()
        }

        Assertions.assertTrue(didCall.get())
    }

    @Test
    fun asyncCallbackDoesNotTriggerActionForWrongJobID() = runTest {
        val jobID = JobID(123456)
        val callback = CallbackForTest(uniqueID = UUID.randomUUID()).apply {
            this.jobID = jobID
        }

        val didCall = AtomicBoolean(false)

        mgr.subscribe<CallbackForTest>(jobID = JobID(123)) { _ ->
            delay(0.milliseconds) // yield
            didCall.set(true)
        }.use {
            client.postCallback(callback)
            mgr.runWaitCallbackAsync()
        }

        Assertions.assertFalse(didCall.get())
    }

    @Test
    fun asyncCallbackIsBlockedBySyncRunCallbacks() = runTest {
        val callback = CallbackForTest(uniqueID = UUID.randomUUID())

        val didCall = AtomicBoolean(false)

        mgr.subscribe<CallbackForTest> { cb ->
            delay(50.milliseconds)
            Assertions.assertEquals(callback.uniqueID, cb.uniqueID)
            didCall.set(true)
        }.use {
            client.postCallback(callback)
            mgr.runCallbacks()
        }

        Assertions.assertTrue(didCall.get())
    }

    @Test
    fun asyncSubscribedFunctionDoesNotRunWhenSubscriptionIsDisposed() = runTest {
        val callback = CallbackForTest()

        val callCount = AtomicInteger(0)

        mgr.subscribe<CallbackForTest> { _ ->
            delay(0.milliseconds) // yield
            callCount.incrementAndGet()
        }.use {
            client.postCallback(callback)
            mgr.runWaitCallbackAsync()
        }

        client.postCallback(callback)
        mgr.runWaitCallbackAsync()

        Assertions.assertEquals(1, callCount.get())
    }

    @Test
    fun mixedSyncAndAsyncSubscribersBothTrigger() = runTest {
        val callback = CallbackForTest(uniqueID = UUID.randomUUID())

        val syncCalled = AtomicBoolean(false)
        val asyncCalled = AtomicBoolean(false)

        val s1 = mgr.subscribe<CallbackForTest> { cb ->
            Assertions.assertEquals(callback.uniqueID, cb.uniqueID)
            syncCalled.set(true)
        }

        val s2 = mgr.subscribe<CallbackForTest> { cb ->
            delay(0.milliseconds) // yield
            Assertions.assertEquals(callback.uniqueID, cb.uniqueID)
            asyncCalled.set(true)
        }

        s1.use {
            s2.use {
                client.postCallback(callback)
                mgr.runWaitCallbackAsync()
            }
        }

        Assertions.assertTrue(syncCalled.get())
        Assertions.assertTrue(asyncCalled.get())
    }

    @Test
    fun asyncCallbackExceptionPropagatesOnAsyncPath() = runTest {
        val callback = CallbackForTest(uniqueID = UUID.randomUUID())

        mgr.subscribe<CallbackForTest> { _ ->
            delay(0.milliseconds) // yield
            throw IllegalStateException("test exception")
        }.use {
            client.postCallback(callback)

            val ex = Assertions.assertThrows(IllegalStateException::class.java) {
                kotlinx.coroutines.runBlocking { mgr.runWaitCallbackAsync() }
            }
            Assertions.assertEquals("test exception", ex.message)
        }
    }

    @Test
    fun asyncCallbackExceptionWrappedOnSyncPath() {
        val callback = CallbackForTest(uniqueID = UUID.randomUUID())

        mgr.subscribe<CallbackForTest> { _ ->
            delay(0.milliseconds) // yield
            throw IllegalStateException("test exception")
        }.use {
            client.postCallback(callback)

            val ex = Assertions.assertThrows(IllegalStateException::class.java) {
                mgr.runCallbacks()
            }
            Assertions.assertEquals("test exception", ex.message)
        }
    }

    class CallbackForTest(val uniqueID: UUID? = null) : CallbackMsg()
}
