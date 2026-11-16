package in.dragonbra.javasteam.util;

import com.github.luben.zstd.Zstd;
import in.dragonbra.javasteam.TestBase;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VZstdUtilTest extends TestBase {

    /**
     * The decrypted payload of depot/depot_3441461_chunk_9e72678e305540630a665b93e1463bc3983eb55a.bin,
     * which is the VZstd data as Steam serves it.
     */
    private static final String REAL_CHUNK = "depot/depot_3441461_chunk_9e72678e305540630a665b93e1463bc3983eb55a.vzstd";

    private static final int REAL_CHUNK_SIZE = 156;

    private static final String REAL_CHUNK_SHA1 = "9E72678E305540630A665B93E1463BC3983EB55A";

    // Disabled: the full-size real chunk is not in the repository, because it is licensed third-party
    // content (a 729,103 byte slice of the VC 2022 Redist that decompresses to 1 MB). To run this
    // locally, fetch it and place it under src/test/resources/depot without committing it.
    // App 228980 (Steamworks Common Redistributables), depot 228989,
    // manifest 5753583882400741046, chunk f6da62a3fd9d7a92184404b5752537918965037c.
    // A chunk ID is the SHA-1 of the decompressed chunk.
    // private static final String FULL_CHUNK = "depot/depot_228989_chunk_f6da62a3fd9d7a92184404b5752537918965037c.vzstd";

    // private static final int FULL_CHUNK_SIZE = 1024 * 1024;

    // private static final String FULL_CHUNK_SHA1 = "F6DA62A3FD9D7A92184404B5752537918965037C";

    @Test
    public void decompressesRealChunk() throws IOException, NoSuchAlgorithmException {
        var data = readResource(REAL_CHUNK);
        var destination = new byte[REAL_CHUNK_SIZE];

        var written = VZstdUtil.decompress(data, destination, true);

        Assertions.assertEquals(REAL_CHUNK_SIZE, written);
        Assertions.assertEquals(REAL_CHUNK_SHA1, sha1(destination));
    }

    // @Test
    // public void decompressesFullSizeRealChunk() throws IOException, NoSuchAlgorithmException {
    //     var data = readResource(FULL_CHUNK);
    //     Assertions.assertEquals(729103, data.length);
    //     var destination = new byte[FULL_CHUNK_SIZE];
    //
    //     // Twice, so the second pass runs on a buffer taken back from the pool.
    //     for (int i = 0; i < 2; i++) {
    //         Arrays.fill(destination, (byte) 0);
    //         var written = VZstdUtil.decompress(data, destination, true);
    //
    //         Assertions.assertEquals(FULL_CHUNK_SIZE, written);
    //         Assertions.assertEquals(FULL_CHUNK_SHA1, sha1(destination));
    //     }
    // }

    @Test
    public void decompressesRealChunkRepeatedly() throws IOException, NoSuchAlgorithmException {
        var data = readResource(REAL_CHUNK);

        // Every call after the first one gets its input buffer back from the pool.
        for (int i = 0; i < 100; i++) {
            var destination = new byte[REAL_CHUNK_SIZE];
            Assertions.assertEquals(REAL_CHUNK_SIZE, VZstdUtil.decompress(data, destination, true));
            Assertions.assertEquals(REAL_CHUNK_SHA1, sha1(destination));
        }
    }

    @Test
    public void honorsLengthWhenBufferIsLarger() throws IOException, NoSuchAlgorithmException {
        var data = readResource(REAL_CHUNK);
        var padded = Arrays.copyOf(data, data.length + 64);
        Arrays.fill(padded, data.length, padded.length, (byte) 0x7F);
        var destination = new byte[REAL_CHUNK_SIZE + 32];

        var written = VZstdUtil.decompress(padded, destination, true, data.length);

        Assertions.assertEquals(REAL_CHUNK_SIZE, written);
        Assertions.assertEquals(REAL_CHUNK_SHA1, sha1(Arrays.copyOf(destination, written)));
    }

    @Test
    public void decompressesChunkLargerThanTheInputBuffer() throws IOException {
        // 1 MB like a full depot chunk. The compressed data spans several fills of the pooled
        // input buffer and the output spans several 64 KB reads.
        var expected = chunkContent(1024 * 1024, 42);
        var data = wrap(expected);
        Assertions.assertTrue(data.length > 4 * 128 * 1024, "compressed data should span several input buffers");

        var destination = new byte[expected.length];
        var written = VZstdUtil.decompress(data, destination, true);

        Assertions.assertEquals(expected.length, written);
        Assertions.assertArrayEquals(expected, destination);
    }

    @Test
    public void decompressesInParallel() throws Exception {
        var threads = 8;
        var executor = Executors.newFixedThreadPool(threads);
        try {
            var futures = new ArrayList<Future<Boolean>>();
            for (int t = 0; t < threads; t++) {
                // Different content per thread, so a buffer shared between two streams shows up as wrong bytes.
                var expected = chunkContent(300_000 + t * 10_000, t);
                var data = wrap(expected);
                Callable<Boolean> task = () -> {
                    var destination = new byte[expected.length];
                    for (int i = 0; i < 50; i++) {
                        Arrays.fill(destination, (byte) 0);
                        if (VZstdUtil.decompress(data, destination, true) != expected.length ||
                                !Arrays.equals(expected, destination)) {
                            return false;
                        }
                    }
                    return true;
                };
                futures.add(executor.submit(task));
            }

            for (var future : futures) {
                Assertions.assertTrue(future.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void recoversAfterCorruptData() throws IOException {
        var expected = chunkContent(400_000, 7);
        var data = wrap(expected);
        var destination = new byte[expected.length];

        // Break the Zstd frame magic, so the stream fails on its first read.
        var badFrame = data.clone();
        badFrame[8] ^= 0x55;
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(badFrame, destination, false));

        // Damage in the middle of the frame, after the stream has already produced output.
        var corrupt = data.clone();
        Arrays.fill(corrupt, 200_000, 200_064, (byte) 0xFF);
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(corrupt, destination, true));

        // Keep the footer, drop the second half of the frame.
        var cut = data.length / 2;
        var shortened = new byte[cut + 15];
        System.arraycopy(data, 0, shortened, 0, cut);
        System.arraycopy(data, data.length - 15, shortened, cut, 15);
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(shortened, destination, false));

        // The buffer handed back by the failed streams must still be usable.
        Arrays.fill(destination, (byte) 0);
        Assertions.assertEquals(expected.length, VZstdUtil.decompress(data, destination, true));
        Assertions.assertArrayEquals(expected, destination);
    }

    @Test
    public void rejectsBadHeaderFooterAndSizes() throws IOException {
        var data = readResource(REAL_CHUNK);
        var destination = new byte[REAL_CHUNK_SIZE];

        var badHeader = data.clone();
        badHeader[0] = 'X';
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(badHeader, destination));

        var badFooter = data.clone();
        badFooter[badFooter.length - 1] = 'X';
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(badFooter, destination));

        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(new byte[22], destination));
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> VZstdUtil.decompress(data, new byte[REAL_CHUNK_SIZE - 1])
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> VZstdUtil.decompress(data, destination, false, data.length + 1)
        );

        var badCrc = data.clone();
        badCrc[badCrc.length - 15] ^= 0x01;
        Assertions.assertThrows(IOException.class, () -> VZstdUtil.decompress(badCrc, destination, true));
    }

    private byte[] readResource(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name)) {
            Assertions.assertNotNull(stream, name);
            return stream.readAllBytes();
        }
    }

    private static String sha1(byte[] data) throws NoSuchAlgorithmException {
        return Hex.encodeHexString(MessageDigest.getInstance("SHA-1").digest(data), false);
    }

    /**
     * Pseudo random bytes that compress to about three quarters of their size.
     */
    private static byte[] chunkContent(int size, long seed) {
        var content = new byte[size];
        new Random(seed).nextBytes(content);
        for (int i = 0; i < content.length; i++) {
            content[i] &= 0x3F;
        }
        return content;
    }

    /**
     * Wraps data the way Steam does: "VSZa", CRC32, the Zstd frame, CRC32, decompressed size, 4 unused bytes, "zsv".
     */
    private static byte[] wrap(byte[] content) {
        var frame = Zstd.compress(content, 3);
        var crc = (int) Utils.crc32(content);
        var buffer = ByteBuffer.allocate(8 + frame.length + 15).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(0x615A5356);
        buffer.putInt(crc);
        buffer.put(frame);
        buffer.putInt(crc);
        buffer.putInt(content.length);
        buffer.putInt(0);
        buffer.put(new byte[]{'z', 's', 'v'});
        return buffer.array();
    }
}
