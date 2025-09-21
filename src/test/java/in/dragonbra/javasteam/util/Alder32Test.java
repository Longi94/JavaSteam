package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

public class Alder32Test {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void returnsCorrectWhenEmpty(int input) {
        var emptyArray = new byte[0];
        var result = Adler32.calculate(input, emptyArray);
        Assertions.assertEquals(input, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 8, 215, 1024, 1024 + 15, 2034, 4096})
    void matchesReference(int length) {
        final var seed = 0;
        var data = new byte[length];
        new Random().nextBytes(data);

        var expected = referenceImplementation(data);
        var actual = Adler32.calculate(seed, data);

        Assertions.assertEquals(expected, actual);
    }

    // Additional benchmarking

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void benchmarkSmallBuffers() {
        final int iterations = 100_000;
        final int bufferSize = 1024; // 1KB buffers

        var data = new byte[bufferSize];
        new Random(12345).nextBytes(data); // Fixed seed for consistency

        // Warmup
        for (int i = 0; i < 1000; i++) {
            referenceImplementation(data);
            Adler32.calculate(data);
        }

        // Benchmark reference implementation
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            referenceImplementation(data);
        }
        long referenceTime = System.nanoTime() - startTime;

        // Benchmark optimized implementation
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Adler32.calculate(data);
        }
        long optimizedTime = System.nanoTime() - startTime;

        double speedup = (double) referenceTime / optimizedTime;

        System.out.printf("Small Buffer Benchmark (%d bytes, %d iterations):%n", bufferSize, iterations);
        System.out.printf("Reference implementation: %.2f ms%n", referenceTime / 1_000_000.0);
        System.out.printf("Optimized implementation: %.2f ms%n", optimizedTime / 1_000_000.0);
        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.println();

        // Assert that optimized version is faster (allow for some measurement variance)
        Assertions.assertTrue(speedup > 0.8,
                String.format("Expected optimized version to be competitive, but speedup was only %.2fx", speedup));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void benchmarkLargeBuffers() {
        final int iterations = 1_000;
        final int bufferSize = 1024 * 1024; // 1MB buffers

        var data = new byte[bufferSize];
        new Random(54321).nextBytes(data); // Fixed seed for consistency

        // Warmup
        for (int i = 0; i < 10; i++) {
            referenceImplementation(data);
            Adler32.calculate(data);
        }

        // Benchmark reference implementation
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            referenceImplementation(data);
        }
        long referenceTime = System.nanoTime() - startTime;

        // Benchmark optimized implementation
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Adler32.calculate(data);
        }
        long optimizedTime = System.nanoTime() - startTime;

        double speedup = (double) referenceTime / optimizedTime;

        System.out.printf("Large Buffer Benchmark (%d bytes, %d iterations):%n", bufferSize, iterations);
        System.out.printf("Reference implementation: %.2f ms%n", referenceTime / 1_000_000.0);
        System.out.printf("Optimized implementation: %.2f ms%n", optimizedTime / 1_000_000.0);
        System.out.printf("Speedup: %.2fx%n", speedup);
        System.out.println();

        // Assert that optimized version is significantly faster for large buffers
        Assertions.assertTrue(speedup > 1.0,
                String.format("Expected optimized version to be faster for large buffers, but speedup was only %.2fx", speedup));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void benchmarkVariousSizes() {
        final int[] bufferSizes = {16, 64, 256, 1024, 4096, 16384, 65536};
        final int baseIterations = 50_000;

        System.out.println("Performance Comparison Across Buffer Sizes:");
        System.out.println("Size\t\tReference(ms)\tOptimized(ms)\tSpeedup");
        System.out.println("----\t\t-------------\t-------------\t-------");

        for (int size : bufferSizes) {
            var data = new byte[size];
            new Random(size).nextBytes(data); // Different seed per size

            // Adjust iterations based on buffer size to keep test time reasonable
            int iterations = Math.max(1000, baseIterations / (size / 16));

            // Warmup
            for (int i = 0; i < Math.min(100, iterations / 10); i++) {
                referenceImplementation(data);
                Adler32.calculate(data);
            }

            // Benchmark reference
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                referenceImplementation(data);
            }
            long referenceTime = System.nanoTime() - startTime;

            // Benchmark optimized
            startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                Adler32.calculate(data);
            }
            long optimizedTime = System.nanoTime() - startTime;

            double refMs = referenceTime / 1_000_000.0;
            double optMs = optimizedTime / 1_000_000.0;
            double speedup = (double) referenceTime / optimizedTime;

            System.out.printf("%d\t\t%.2f\t\t%.2f\t\t%.2fx%n", size, refMs, optMs, speedup);
        }
        System.out.println();
    }

    private static int referenceImplementation(byte[] input) {
        int a = 0, b = 0;
        for (byte value : input) {
            // Use bitwise AND with 0xFF to treat byte as unsigned
            a = (a + (value & 0xFF)) % 65521;
            b = (b + a) % 65521;
        }

        return a | (b << 16);
    }
}
