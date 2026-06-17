package in.dragonbra.javasteam.util.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class ScheduledFunctionTest {

    @Test
    void startInvokesFunction() throws InterruptedException {
        var latch = new CountDownLatch(3);
        var func = new ScheduledFunction(latch::countDown, 50L);

        func.start();
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        func.stop();

        Assertions.assertTrue(completed, "Function was not invoked 3 times within timeout");
    }

    @Test
    void stopPreventsInvocations() throws InterruptedException {
        var count = new AtomicInteger();
        var func = new ScheduledFunction(count::incrementAndGet, 50L);

        func.start();
        Thread.sleep(150);
        func.stop();

        int countAfterStop = count.get();
        Thread.sleep(150);

        Assertions.assertEquals(countAfterStop, count.get(), "Function was invoked after stop()");
    }

    @Test
    void startIsIdempotent() throws InterruptedException {
        var latch = new CountDownLatch(3);
        var func = new ScheduledFunction(latch::countDown, 50L);

        func.start();
        func.start();
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        func.stop();

        Assertions.assertTrue(completed);
    }

    @Test
    void stopWhenNotStartedDoesNotThrow() {
        var func = new ScheduledFunction(() -> {}, 100L);
        Assertions.assertDoesNotThrow(func::stop);
    }

    @Test
    void delayGetterAndSetter() {
        var func = new ScheduledFunction(() -> {}, 100L);
        Assertions.assertEquals(100L, func.getDelay());
        func.setDelay(200L);
        Assertions.assertEquals(200L, func.getDelay());
    }
}