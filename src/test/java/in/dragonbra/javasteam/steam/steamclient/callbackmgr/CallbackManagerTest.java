package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class CallbackManagerTest extends TestBase {

    Logger logger = LogManager.getLogger(CallbackManagerTest.class);
    private SteamClient client;
    private CallbackManager mgr;

    @BeforeEach
    public void setUp() {
        client = new SteamClient();
        mgr = new CallbackManager(client);
    }

    @Test
    public void postedCallbackTriggersAction() {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            Assertions.assertEquals(callback.uniqueID, cb.getUniqueID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void postedCallbackTriggersAction_CatchAll() {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            Assertions.assertInstanceOf(CallbackForTest.class, cb);
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void postedCallbackTriggersActionForExplicitJobIDInvalid() {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, JobID.INVALID, cb -> {
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            Assertions.assertEquals(jobID, cb.getJobID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void postedCallbackWithJobIDTriggersActionWhenNoJobIDSpecified() {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            Assertions.assertEquals(jobID, cb.getJobID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void postedCallbackDoesNotTriggerActionForWrongJobID() {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, new JobID(123), cb -> {
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertFalse(didCall.get());
    }

    @Test
    public void postedCallbackWithJobIDTriggersCallbackForJobID() {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);
        try (var ignored = mgr.subscribe(CallbackForTest.class, new JobID(123456), cb -> {
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            Assertions.assertEquals(jobID, cb.getJobID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void subscribedFunctionDoesNotRunWhenSubscriptionIsDisposed() {
        var callback = new CallbackForTest();

        var callCount = new AtomicInteger(0);
        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> callCount.incrementAndGet())) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }
        postAndRunCallback(callback);

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    public void postedCallbacksTriggerActions() {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var numCallbacksRun = new AtomicInteger(0);
        try (var ignored = mgr.subscribe(
                CallbackForTest.class, cb -> {
                    Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
                    numCallbacksRun.incrementAndGet();
                })
        ) {
            for (int i = 0; i < 10; i++) {
                client.postCallback(callback);
            }

            mgr.runWaitAllCallbacks(0L);
            Assertions.assertEquals(10, numCallbacksRun.get());

            // Callbacks should have been freed.
            mgr.runWaitAllCallbacks(0L);
            Assertions.assertEquals(10, numCallbacksRun.get());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Test
    public void postedCallbacksTriggerActionsAsync() throws Exception {
        var callbacks = new CallbackForTest[10];
        for (int i = 0; i < callbacks.length; i++) {
            callbacks[i] = new CallbackForTest();
            callbacks[i].setUniqueID(UUID.randomUUID());
        }

        var numCallbacksRun = new AtomicInteger(0);

        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            int index = numCallbacksRun.get();
            Assertions.assertTrue(index < callbacks.length);
            Assertions.assertEquals(callbacks[index].getUniqueID(), cb.getUniqueID());
            numCallbacksRun.incrementAndGet();
        })) {
            for (var callback : callbacks) {
                client.postCallback(callback);
            }

            for (int i = 1; i <= callbacks.length; i++) {
                mgr.runWaitCallbacks();
                Assertions.assertEquals(i, numCallbacksRun.get());
            }

            // Callbacks should have been freed.
            mgr.runWaitAllCallbacks(0L);
            Assertions.assertEquals(10, numCallbacksRun.get());
        }
    }

    @Test
    public void correctlyUnsubscribesFromInsideOfCallback() throws IOException {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        try (var s1 = mgr.subscribe(CallbackForTest.class, cb -> { /* nothing */ })) {
            var subscription = new AtomicReference<Closeable>();

            subscription.set(mgr.subscribe(CallbackForTest.class, cb -> {
                Assertions.assertNotNull(subscription.get());
                try {
                    subscription.get().close();
                } catch (IOException e) {
                    logger.error(e);
                }
                subscription.set(null);
            }));

            postAndRunCallback(callback);
            Assertions.assertNull(subscription.get());
        }
    }

    @Test
    public void correctlySubscribesFromInsideOfCallback() {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        try (
                var s1 = mgr.subscribe(CallbackForTest.class, cb -> { /* nothing */ });
                var se = mgr.subscribe(CallbackForTest.class, cb -> {
                    try (var s2 = mgr.subscribe(CallbackForTest.class, cb2 -> { /* nothing */ })) {
                        // subscribed and immediately disposed within callback
                    } catch (IOException e) {
                        logger.error(e);
                    }
                })
        ) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    @Test
    public void correctlyAwaitsForAsyncCallbacks() throws Exception {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var numCallbacksRun = new AtomicInteger(0);

        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            numCallbacksRun.incrementAndGet();
        })) {
            for (int i = 0; i < 10; i++) {
                client.postCallback(callback);
            }

            for (int i = 1; i <= 10; i++) {
                mgr.runWaitCallbacks();
                Assertions.assertEquals(i, numCallbacksRun.get());
            }

            mgr.runWaitAllCallbacks(0L);
            Assertions.assertEquals(10, numCallbacksRun.get());
        }
    }

    @Test
    public void asyncCallbackWithJobIDTriggersAction() throws IOException {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (var ignored = mgr.subscribe(CallbackForTest.class, new JobID(123456), cb -> {
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            Assertions.assertEquals(jobID, cb.getJobID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void asyncCallbackDoesNotTriggerActionForWrongJobID() throws IOException {
        var jobID = new JobID(123456);
        var callback = new CallbackForTest();
        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (var ignored = mgr.subscribe(CallbackForTest.class, new JobID(123), cb -> {
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        }

        Assertions.assertFalse(didCall.get());
    }

    @Test
    public void asyncCallbackIsBlockedBySyncRunCallbacks() throws IOException {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
            didCall.set(true);
        })) {
            postAndRunCallback(callback);
        }

        Assertions.assertTrue(didCall.get());
    }

    @Test
    public void asyncSubscribedFunctionDoesNotRunWhenSubscriptionIsDisposed() throws IOException {
        var callback = new CallbackForTest();

        var callCount = new AtomicInteger(0);

        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> callCount.incrementAndGet())) {
            postAndRunCallback(callback);
        }
        postAndRunCallback(callback);

        Assertions.assertEquals(1, callCount.get());
    }

    @Test
    public void mixedSyncAndAsyncSubscribersBothTrigger() throws IOException {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var syncCalled = new AtomicBoolean(false);
        var asyncCalled = new AtomicBoolean(false);

        try (
                var s1 = mgr.subscribe(CallbackForTest.class, cb -> {
                    Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
                    syncCalled.set(true);
                });
                var s2 = mgr.subscribe(CallbackForTest.class, cb -> {
                    Assertions.assertEquals(callback.getUniqueID(), cb.getUniqueID());
                    asyncCalled.set(true);
                })
        ) {
            postAndRunCallback(callback);
        }

        Assertions.assertTrue(syncCalled.get());
        Assertions.assertTrue(asyncCalled.get());
    }

    @Test
    public void asyncCallbackExceptionPropagatesOnSyncPath() {
        var callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        try (var ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            throw new RuntimeException("test exception");
        })) {
            client.postCallback(callback);

            var ex = Assertions.assertThrows(
                    RuntimeException.class,
                    () -> mgr.runCallbacks()
            );
            Assertions.assertEquals("test exception", ex.getMessage());
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private void postAndRunCallback(CallbackMsg callback) {
        client.postCallback(callback);
        mgr.runCallbacks();
    }

    public static class CallbackForTest extends CallbackMsg {

        private UUID uniqueID;

        public CallbackForTest() {
        }

        public UUID getUniqueID() {
            return uniqueID;
        }

        public void setUniqueID(UUID uuid) {
            this.uniqueID = uuid;
        }
    }

}
