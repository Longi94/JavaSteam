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
        CallbackForTest callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, cb -> {
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
        final CallbackForTest callback = new CallbackForTest();
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, cb -> {
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
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest();

        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, JobID.INVALID, cb -> {
            Assertions.assertEquals(jobID, cb.getJobID());
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
    public void postedCallbackWithJobIDTriggersActionWhenNoJobIDSpecified() {
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest();

        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, cb -> {
            Assertions.assertEquals(jobID, cb.getJobID());
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
    public void postedCallbackDoesNotTriggerActionForWrongJobID() {
        JobID jobID = new JobID(123456);
        CallbackForTest callback = new CallbackForTest();

        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, new JobID(123), cb -> {
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
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest();

        callback.setJobID(jobID);
        callback.setUniqueID(UUID.randomUUID());

        var didCall = new AtomicBoolean(false);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, new JobID(123456), cb -> {
            Assertions.assertEquals(jobID, cb.getJobID());
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
    public void subscribedFunctionDoesNotRunWhenSubscriptionIsDisposed() {
        CallbackForTest callback = new CallbackForTest();

        var callCount = new AtomicInteger(0);

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, cb -> callCount.incrementAndGet())) {
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

            mgr.runWaitAllCallbacks(1L); // We must provide `some` sort of timeout or null will always happen on 0L
            Assertions.assertEquals(10, numCallbacksRun.get());

            // Callbacks should have been freed.
            mgr.runWaitAllCallbacks(0L);
            Assertions.assertEquals(10, numCallbacksRun.get());
        } catch (Exception e) {
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
