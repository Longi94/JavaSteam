package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

public class Adler32Test {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void returnsCorrectWhenEmpty(int input) {
        var emptyArray = new byte[0];
        var result = Adler32.calculate(input, emptyArray);
        Assertions.assertEquals(input, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 8, 15, 16, 17, 215, 1024, 1024 + 15, 2034, 4096, 5552 - 1, 5552, 5552 + 1, 5552 + 16, 5552 * 2})
    void matchesReference(int length) {
        System.out.println("Using length: " + length);
        final var seed = 0;
        var data = new byte[length];
        new Random().nextBytes(data);

        var expected = referenceImplementation(seed, data);
        var actual = Adler32.calculate(seed, data);

        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 15, 16, 17, 1024, 5552 - 1, 5552, 5552 + 1, 5552 * 2 + 3})
    void rangeMatchesCopyOfRange(int length) {
        final var seed = 0;
        final var offset = 7;
        var data = new byte[offset + length + 11];
        new Random().nextBytes(data);

        var expected = referenceImplementation(seed, Arrays.copyOfRange(data, offset, offset + length));
        var actual = Adler32.calculate(seed, data, offset, length);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void rangeOutsideBufferThrows() {
        var data = new byte[16];

        Assertions.assertThrows(IllegalArgumentException.class, () -> Adler32.calculate(0, data, 8, 9));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Adler32.calculate(0, data, -1, 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "Wikipedia", "123456789", "SteamKit is good software, you should use it. :)"})
    void matchesKnownVectors(String input) {
        final int seed = 1;
        final int[] expectedValues = {0x00620062, 0x11E60398, 0x091e01de, 0xaf8110e6};
        final String[] testStrings = {"a", "Wikipedia", "123456789", "SteamKit is good software, you should use it. :)"};

        // Find the expected value for this input string
        int expectedIndex = -1;
        for (int i = 0; i < testStrings.length; i++) {
            if (testStrings[i].equals(input)) {
                expectedIndex = i;
                break;
            }
        }

        var data = input.getBytes(StandardCharsets.US_ASCII);
        var actual = Adler32.calculate(seed, data);
        var expected = expectedValues[expectedIndex];

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void noOverflowWithMaxBytes() {
        var data = new byte[5552]; // NMAX value
        Arrays.fill(data, (byte) 0xFF);

        var expected = referenceImplementation(1, data);
        var actual = Adler32.calculate(1, data);

        Assertions.assertEquals(expected, actual);
    }

    /** Byte by byte, with both halves of the seed, the way the checksum is defined. */
    private static int seededReference(int seed, byte[] input, int offset, int length) {
        long a = seed & 0xFFFF;
        long b = (seed >>> 16) & 0xFFFF;

        for (int i = offset; i < offset + length; i++) {
            a = (a + (input[i] & 0xFF)) % 65521;
            b = (b + a) % 65521;
        }

        return (int) ((b << 16) | a);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0,                  // what Steam uses
            1,                  // standard Adler-32
            0x00010000,         // high half only
            0x12345678,         // both halves
            0xFFF0FFF0,         // 65520, the largest reduced value, in both halves
            0xFFFFFFFF,         // halves that are not reduced
            0xFFF10000,         // 65521 in the high half
    })
    void matchesReferenceForAnySeed(int seed) {
        // Lengths around the modulus, and a chunk-sized one that is several times above it.
        for (int length : new int[]{1, 2, 255, 65520, 65521, 65522, 2 * 65521, 1024 * 1024, 1024 * 1024 + 13}) {
            var data = new byte[length + 5];
            new Random(length).nextBytes(data);

            Assertions.assertEquals(
                    seededReference(seed, data, 5, length),
                    Adler32.calculate(seed, data, 5, length),
                    "seed " + Integer.toHexString(seed) + ", length " + length
            );
        }
    }

    @Test
    void matchesReferenceForWorstCaseData() {
        // All 0xFF gives the largest sums.
        var data = new byte[1024 * 1024];
        Arrays.fill(data, (byte) 0xFF);

        for (int seed : new int[]{0, 1, 0xFFF0FFF0, 0xFFFFFFFF}) {
            Assertions.assertEquals(seededReference(seed, data, 0, data.length), Adler32.calculate(seed, data));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void continuingAChecksumMatchesOnePass(int seed) {
        var data = new byte[300_000];
        new Random(99).nextBytes(data);

        var expected = Adler32.calculate(seed, data);

        for (int split : new int[]{0, 1, 16, 5552, 65521, 150_000, data.length - 1, data.length}) {
            var first = Adler32.calculate(seed, data, 0, split);
            var actual = Adler32.calculate(first, data, split, data.length - split);

            Assertions.assertEquals(expected, actual, "split at " + split);
        }
    }

    @Test
    void seedOneMatchesZlib() {
        var data = new byte[70_000];
        new Random(7).nextBytes(data);

        var zlib = new java.util.zip.Adler32();
        zlib.update(data, 3, 60_000);

        Assertions.assertEquals((int) zlib.getValue(), Adler32.calculate(1, data, 3, 60_000));
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
            for (int i = 0; i < 100; i++) {
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

    @SuppressWarnings("UnusedReturnValue")
    private static int referenceImplementation(byte[] input) {
        return referenceImplementation(0, input);
    }

    private static int referenceImplementation(int seed, byte[] input) {
        var a = seed;
        int b = 0;

        for (byte value : input) {
            // Use bitwise AND with 0xFF to treat byte as unsigned
            a = (a + (value & 0xFF)) % 65521;
            b = (b + a) % 65521;
        }

        return a | (b << 16);
    }
}
