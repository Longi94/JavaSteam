package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class CallbackManagerTest extends TestBase {

    private SteamClient client;
    private CallbackManager mgr;

    @BeforeEach
    public void setUp() {
        client = new SteamClient();
        mgr = new CallbackManager(client);
    }

    @Test
    public void postedCallbackTriggersAction() {
        CallbackForTest callback = new CallbackForTest(UUID.randomUUID());

        final boolean[] didCall = {false};

        Consumer<CallbackForTest> action = cb -> didCall[0] = true;

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(didCall[0]);
    }

    @Test
    public void postedCallbackTriggersAction_CatchAll() {
        final CallbackForTest callback = new CallbackForTest(UUID.randomUUID());

        final boolean[] didCall = {false};

        Consumer<CallbackMsg> action = cb -> {
            assertInstanceOf(CallbackForTest.class, cb);
            CallbackForTest cft = (CallbackForTest) cb;
            assertEquals(callback.getUuid(), cft.getUuid());
            didCall[0] = true;
        };

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(didCall[0]);
    }

    @Test
    public void postedCallbackTriggersActionForExplicitJobIDInvalid() {
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest(UUID.randomUUID());
        callback.setJobID(jobID);

        final boolean[] didCall = {false};

        Consumer<CallbackForTest> action = cb -> {
            assertEquals(jobID, cb.getJobID());
            assertEquals(callback.getUuid(), cb.getUuid());
            didCall[0] = true;
        };

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, JobID.INVALID, action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(didCall[0]);
    }

    @Test
    public void postedCallbackWithJobIDTriggersActionWhenNoJobIDSpecified() {
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest(UUID.randomUUID());
        callback.setJobID(jobID);

        final boolean[] didCall = {false};

        Consumer<CallbackForTest> action = cb -> {
            assertEquals(jobID, cb.getJobID());
            assertEquals(callback.getUuid(), cb.getUuid());
            didCall[0] = true;
        };

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(didCall[0]);
    }

    @Test
    public void postedCallbackDoesNotTriggerActionForWrongJobID() {
        JobID jobID = new JobID(123456);
        CallbackForTest callback = new CallbackForTest(UUID.randomUUID());
        callback.setJobID(jobID);

        final boolean[] didCall = {false};

        Consumer<CallbackForTest> action = cb -> didCall[0] = true;

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, new JobID(123), action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertFalse(didCall[0]);
    }

    @Test
    public void postedCallbackWithJobIDTriggersCallbackForJobID() {
        final JobID jobID = new JobID(123456);
        final CallbackForTest callback = new CallbackForTest(UUID.randomUUID());
        callback.setJobID(jobID);

        final boolean[] didCall = {false};

        Consumer<CallbackForTest> action = cb -> {
            assertEquals(jobID, cb.getJobID());
            assertEquals(callback.getUuid(), cb.getUuid());
            didCall[0] = true;
        };

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, new JobID(123456), action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(didCall[0]);
    }

    @Test
    public void subscribedFunctionDoesNotRunWhenSubscriptionIsDisposed() {
        CallbackForTest callback = new CallbackForTest();

        final int[] callCount = {0};

        Consumer<CallbackForTest> action = cb -> callCount[0]++;

        try (Closeable ignored = mgr.subscribe(CallbackForTest.class, action)) {
            postAndRunCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
        postAndRunCallback(callback);

        assertEquals(1, callCount[0]);
    }

    @Test
    public void postedCallbacksTriggerActions() {
        var callback = new CallbackForTest(UUID.randomUUID());

        var numCallbacksRun = new AtomicInteger(0);

        try (var ignored = mgr.subscribe(
                CallbackForTest.class, cb -> {
                    assertEquals(callback.getUuid(), cb.getUuid());
                    numCallbacksRun.incrementAndGet();
                })
        ) {
            for (int i = 0; i < 10; i++) {
                client.postCallback(callback);
            }

            mgr.runWaitAllCallbacks(1L); // We must provide `some` sort of timeout or null will always happen on 0L
            assertEquals(10, numCallbacksRun.get());

            // Callbacks should have been freed.
            mgr.runWaitAllCallbacks(0L);
            assertEquals(10, numCallbacksRun.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postAndRunCallback(CallbackMsg callback) {
        client.postCallback(callback);
        mgr.runCallbacks();
    }
}
