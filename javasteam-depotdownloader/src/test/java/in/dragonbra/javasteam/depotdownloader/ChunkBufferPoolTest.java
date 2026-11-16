package in.dragonbra.javasteam.depotdownloader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChunkBufferPoolTest {

    @Test
    public void rentsABufferOfTheStandardSize() {
        var pool = new ChunkBufferPool(4);

        Assertions.assertEquals(ChunkBufferPool.BUFFER_SIZE, pool.rent(1).length);
        Assertions.assertEquals(ChunkBufferPool.BUFFER_SIZE, pool.rent(1024 * 1024).length);
        Assertions.assertEquals(ChunkBufferPool.BUFFER_SIZE, pool.rent(ChunkBufferPool.BUFFER_SIZE).length);
    }

    @Test
    public void reusesABufferThatWasGivenBack() {
        var pool = new ChunkBufferPool(4);

        var first = pool.rent(100);
        pool.giveBack(first);

        Assertions.assertEquals(1, pool.getIdleCount());
        Assertions.assertSame(first, pool.rent(200_000));
        Assertions.assertEquals(0, pool.getIdleCount());

        // The pool is empty again, so this one is new.
        Assertions.assertNotSame(first, pool.rent(100));
    }

    @Test
    public void aRequestAboveTheStandardSizeGetsItsOwnBufferWhichIsNotKept() {
        var pool = new ChunkBufferPool(4);

        var large = pool.rent(ChunkBufferPool.BUFFER_SIZE + 1);
        Assertions.assertEquals(ChunkBufferPool.BUFFER_SIZE + 1, large.length);

        pool.giveBack(large);
        Assertions.assertEquals(0, pool.getIdleCount());
    }

    @Test
    public void keepsNoMoreIdleBuffersThanTheCap() {
        var pool = new ChunkBufferPool(2);

        var a = pool.rent(1);
        var b = pool.rent(1);
        var c = pool.rent(1);

        pool.giveBack(a);
        pool.giveBack(b);
        pool.giveBack(c);

        Assertions.assertEquals(2, pool.getIdleCount());
    }

    @Test
    public void clearDropsTheIdleBuffers() {
        var pool = new ChunkBufferPool(2);

        pool.giveBack(pool.rent(1));
        pool.clear();

        Assertions.assertEquals(0, pool.getIdleCount());
    }
}
