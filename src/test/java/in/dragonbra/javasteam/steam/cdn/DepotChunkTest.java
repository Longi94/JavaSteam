package in.dragonbra.javasteam.steam.cdn;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.types.ChunkData;
import in.dragonbra.javasteam.util.Adler32;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DepotChunkTest extends TestBase {

    @Test
    public void decryptsAndDecompressesDepotChunkPKZip() throws IOException, NoSuchAlgorithmException {
        var resource = "depot/depot_440_chunk_bac8e2657470b2eb70d6ddcd6c07004be8738697.bin";

        byte[] chunkData;
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource);
             var ms = new MemoryStream()) {
            IOUtils.copy(stream, ms.asOutputStream());
            chunkData = ms.toByteArray();
        }

        var chunk = new ChunkData(
                new byte[0], // id is not needed here
                2130218374,
                0,
                320,
                544
        );

        var destination = new byte[chunk.getUncompressedLength()];
        var writtenLength = DepotChunk.process(
                chunk,
                chunkData,
                destination,
                new byte[]{
                        (byte) 0x44, (byte) 0xCE, (byte) 0x5C, (byte) 0x52, (byte) 0x97, (byte) 0xA4, (byte) 0x15, (byte) 0xA1,
                        (byte) 0xA6, (byte) 0xF6, (byte) 0x9C, (byte) 0x85, (byte) 0x60, (byte) 0x37, (byte) 0xA5, (byte) 0xA2,
                        (byte) 0xFD, (byte) 0xD8, (byte) 0x2C, (byte) 0xD4, (byte) 0x74, (byte) 0xFA, (byte) 0x65, (byte) 0x9E,
                        (byte) 0xDF, (byte) 0xB4, (byte) 0xD5, (byte) 0x9B, (byte) 0x2A, (byte) 0xBC, (byte) 0x55, (byte) 0xFC
                }
        );

        Assertions.assertEquals(chunk.getCompressedLength(), chunkData.length);
        Assertions.assertEquals(chunk.getUncompressedLength(), writtenLength);

        var hash = Hex.encodeHexString(MessageDigest.getInstance("SHA-1").digest(destination), false);

        Assertions.assertEquals("BAC8E2657470B2EB70D6DDCD6C07004BE8738697", hash);
    }

    @Test
    public void decryptsAndDecompressesDepotChunkVZip() throws IOException, NoSuchAlgorithmException {
        var resource = "depot/depot_232250_chunk_7b8567d9b3c09295cdbf4978c32b348d8e76c750.bin";

        byte[] chunkData;
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource);
             var ms = new MemoryStream()) {
            IOUtils.copy(stream, ms.asOutputStream());
            chunkData = ms.toByteArray();
        }

        var chunk = new ChunkData(
                new byte[0], // id is not needed here
                Integer.parseUnsignedInt("2894626744"),
                0,
                304,
                798
        );

        var destination = new byte[chunk.getUncompressedLength()];
        var writtenLength = DepotChunk.process(
                chunk,
                chunkData,
                destination,
                new byte[]{
                        (byte) 0xE5, (byte) 0xF6, (byte) 0xAE, (byte) 0xD5, (byte) 0x5E, (byte) 0x9E, (byte) 0xCE, (byte) 0x42,
                        (byte) 0x9E, (byte) 0x56, (byte) 0xB8, (byte) 0x13, (byte) 0xFB, (byte) 0xF6, (byte) 0xBF, (byte) 0xE9,
                        (byte) 0x24, (byte) 0xF3, (byte) 0xCF, (byte) 0x72, (byte) 0x97, (byte) 0x2F, (byte) 0xDB, (byte) 0xD0,
                        (byte) 0x57, (byte) 0x1F, (byte) 0xFC, (byte) 0xAD, (byte) 0x9F, (byte) 0x2F, (byte) 0x7D, (byte) 0xAA,
                }
        );

        Assertions.assertEquals(chunk.getCompressedLength(), chunkData.length);
        Assertions.assertEquals(chunk.getUncompressedLength(), writtenLength);

        var hash = Hex.encodeHexString(MessageDigest.getInstance("SHA-1").digest(destination), false);

        Assertions.assertEquals("7B8567D9B3C09295CDBF4978C32B348D8E76C750", hash);
    }

    @Test
    public void decryptsAndDecompressesDepotChunkZStd() throws IOException, NoSuchAlgorithmException {
        var resource = "depot/depot_3441461_chunk_9e72678e305540630a665b93e1463bc3983eb55a.bin";

        byte[] chunkData;
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource);
             var ms = new MemoryStream()) {
            IOUtils.copy(stream, ms.asOutputStream());
            chunkData = ms.toByteArray();
        }

        var chunk = new ChunkData(
                new byte[0], // id is not needed here
                Integer.parseUnsignedInt("3753325726"),
                0,
                176,
                156
        );

        var destination = new byte[chunk.getUncompressedLength()];
        var writtenLength = DepotChunk.process(
                chunk,
                chunkData,
                destination,
                new byte[]{
                        (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
                        (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F, (byte) 0x10,
                        (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18,
                        (byte) 0x19, (byte) 0x1A, (byte) 0x1B, (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F, (byte) 0x20
                }
        );

        Assertions.assertEquals(chunk.getCompressedLength(), chunkData.length);
        Assertions.assertEquals(chunk.getUncompressedLength(), writtenLength);

        var hash = Hex.encodeHexString(MessageDigest.getInstance("SHA-1").digest(destination), false);

        Assertions.assertEquals("9E72678E305540630A665B93E1463BC3983EB55A", hash);
    }

    // region In-place decryption and oversized (pooled) buffers

    /** A chunk from the test resources. The SHA-1 of its content is its chunk id, which is in the file name. */
    private static final class Fixture {
        final String name;
        final String resource;
        final ChunkData chunk;
        final byte[] depotKey;
        final String sha1;

        Fixture(String name, String resource, int checksum, int compressedLength, int uncompressedLength, String depotKey, String sha1) {
            this.name = name;
            this.resource = resource;
            this.chunk = new ChunkData(new byte[0], checksum, 0, compressedLength, uncompressedLength);
            this.depotKey = unhex(depotKey);
            this.sha1 = sha1;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static byte[] unhex(String hex) {
        try {
            return Hex.decodeHex(hex);
        } catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static final List<Fixture> FIXTURES = List.of(
            new Fixture(
                    "PKZip",
                    "depot/depot_440_chunk_bac8e2657470b2eb70d6ddcd6c07004be8738697.bin",
                    2130218374, 320, 544,
                    "44CE5C5297A415A1A6F69C856037A5A2FDD82CD474FA659EDFB4D59B2ABC55FC",
                    "BAC8E2657470B2EB70D6DDCD6C07004BE8738697"
            ),
            new Fixture(
                    "LZMA",
                    "depot/depot_232250_chunk_7b8567d9b3c09295cdbf4978c32b348d8e76c750.bin",
                    Integer.parseUnsignedInt("2894626744"), 304, 798,
                    "E5F6AED55E9ECE429E56B813FBF6BFE924F3CF72972FDBD0571FFCAD9F2F7DAA",
                    "7B8567D9B3C09295CDBF4978C32B348D8E76C750"
            ),
            new Fixture(
                    "Zstd",
                    "depot/depot_3441461_chunk_9e72678e305540630a665b93e1463bc3983eb55a.bin",
                    Integer.parseUnsignedInt("3753325726"), 176, 156,
                    "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F20",
                    "9E72678E305540630A665B93E1463BC3983EB55A"
            )
    );

    /** Every chunk kind, on the platform provider and on the Bouncy Castle fallback. */
    static Stream<Arguments> chunksAndProviders() {
        var platform = Security.getProvider("SunJCE");
        Assertions.assertNotNull(platform, "This test expects to run on a JVM with the SunJCE provider.");

        var providers = List.of(platform, new BouncyCastleProvider());

        return FIXTURES.stream().flatMap(fixture ->
                providers.stream().map(provider -> Arguments.of(fixture, provider))
        );
    }

    private byte[] load(Fixture fixture) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(fixture.resource)) {
            Assertions.assertNotNull(stream, fixture.resource);
            var data = IOUtils.toByteArray(stream);
            Assertions.assertEquals(fixture.chunk.getCompressedLength(), data.length);
            return data;
        }
    }

    private static String sha1(byte[] data, int length) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-1");
        digest.update(data, 0, length);
        return Hex.encodeHexString(digest.digest(), false);
    }

    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("chunksAndProviders")
    public void decryptsInPlace(Fixture fixture, Provider provider) throws IOException, NoSuchAlgorithmException {
        var data = load(fixture);
        var destination = new byte[fixture.chunk.getUncompressedLength()];

        var written = DepotChunk.processWithProvider(fixture.chunk, data, data.length, destination, fixture.depotKey, provider);

        Assertions.assertEquals(fixture.chunk.getUncompressedLength(), written);
        Assertions.assertEquals(fixture.sha1, sha1(destination, written));
    }

    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("chunksAndProviders")
    public void processesChunkInBuffersLongerThanTheData(Fixture fixture, Provider provider) throws IOException, NoSuchAlgorithmException {
        var chunkData = load(fixture);

        // What pooled buffers look like: longer than the data, with old content after (and under) it.
        var data = new byte[chunkData.length + 1000];
        Arrays.fill(data, (byte) 0x5A);
        System.arraycopy(chunkData, 0, data, 0, chunkData.length);

        var destination = new byte[fixture.chunk.getUncompressedLength() + 1000];
        Arrays.fill(destination, (byte) 0x5A);

        var written = DepotChunk.processWithProvider(fixture.chunk, data, chunkData.length, destination, fixture.depotKey, provider);

        Assertions.assertEquals(fixture.chunk.getUncompressedLength(), written);
        Assertions.assertEquals(fixture.sha1, sha1(destination, written));

        // Nothing is written past the data, in either buffer.
        for (int i = chunkData.length; i < data.length; i++) {
            Assertions.assertEquals((byte) 0x5A, data[i], "data[" + i + "]");
        }
        for (int i = written; i < destination.length; i++) {
            Assertions.assertEquals((byte) 0x5A, destination[i], "destination[" + i + "]");
        }
    }

    @Test
    public void lengthOverloadUsesOnlyTheGivenLength() throws IOException, NoSuchAlgorithmException {
        // The public overload, with whichever provider the library picks.
        for (var fixture : FIXTURES) {
            var chunkData = load(fixture);
            var data = Arrays.copyOf(chunkData, chunkData.length + 333);
            var destination = new byte[fixture.chunk.getUncompressedLength() + 333];

            var written = DepotChunk.process(fixture.chunk, data, chunkData.length, destination, fixture.depotKey);

            Assertions.assertEquals(fixture.sha1, sha1(destination, written), fixture.name);
        }
    }

    static Stream<Provider> providers() {
        return Stream.of(Security.getProvider("SunJCE"), new BouncyCastleProvider());
    }

    /**
     * The fixtures are a few hundred bytes. Real chunks are up to 1 MiB, which a provider decrypts in
     * several pieces, so this is where an in-place decryption that is not copy-safe would show.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    public void decryptsAFullSizeChunkInPlace(Provider provider) throws Exception {
        var content = new byte[1024 * 1024];
        new Random(1234).nextBytes(content);

        // A PKZip chunk, stored, so the decrypted data is as long as a chunk can be.
        var zipped = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(zipped)) {
            var crc = new CRC32();
            crc.update(content);

            var entry = new ZipEntry("z");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());

            zip.putNextEntry(entry);
            zip.write(content);
            zip.closeEntry();
        }

        var depotKey = FIXTURES.get(0).depotKey;
        var keySpec = new SecretKeySpec(depotKey, "AES");

        var iv = new byte[16];
        new Random(5678).nextBytes(iv);

        var ecb = Cipher.getInstance("AES/ECB/NoPadding");
        ecb.init(Cipher.ENCRYPT_MODE, keySpec);

        var cbc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cbc.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));

        var encrypted = new ByteArrayOutputStream();
        encrypted.write(ecb.doFinal(iv));
        encrypted.write(cbc.doFinal(zipped.toByteArray()));

        var chunkData = encrypted.toByteArray();
        var chunk = new ChunkData(new byte[0], Adler32.calculate(content), 0, chunkData.length, content.length);

        // Exactly sized, and in a longer buffer.
        for (int extra : new int[]{0, 4096}) {
            var data = Arrays.copyOf(chunkData, chunkData.length + extra);
            var destination = new byte[content.length + extra];

            var written = DepotChunk.processWithProvider(chunk, data, chunkData.length, destination, depotKey, provider);

            Assertions.assertEquals(content.length, written);
            Assertions.assertArrayEquals(content, Arrays.copyOf(destination, written));
        }
    }

    @Test
    public void rejectsALengthOutsideTheData() {
        var fixture = FIXTURES.get(0);
        var destination = new byte[fixture.chunk.getUncompressedLength()];

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                DepotChunk.process(fixture.chunk, new byte[320], 321, destination, fixture.depotKey));
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                DepotChunk.process(fixture.chunk, new byte[320], 15, destination, fixture.depotKey));
    }

    // endregion
}
