package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.enums.EDepotFileFlag;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.crypto.CryptoHelper;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

@SuppressWarnings({"resource", "DataFlowIssue"})
public class DepotManifestTest {

    private static final byte[] DEPOT_440_DECRYPTION_KEY = new byte[]{
            (byte) 0x44, (byte) 0xCE, (byte) 0x5C, (byte) 0x52, (byte) 0x97, (byte) 0xA4, (byte) 0x15, (byte) 0xA1,
            (byte) 0xA6, (byte) 0xF6, (byte) 0x9C, (byte) 0x85, (byte) 0x60, (byte) 0x37, (byte) 0xA5, (byte) 0xA2,
            (byte) 0xFD, (byte) 0xD8, (byte) 0x2C, (byte) 0xD4, (byte) 0x74, (byte) 0xFA, (byte) 0x65, (byte) 0x9E,
            (byte) 0xDF, (byte) 0xB4, (byte) 0xD5, (byte) 0x9B, (byte) 0x2A, (byte) 0xBC, (byte) 0x55, (byte) 0xFC
    };


    @Test
    public void parsesAndDecryptsManifestVersion4() throws IOException, NoSuchAlgorithmException {
        var stream = getClass().getResourceAsStream("/depot/depot_440_1118032470228587934_v4.manifest");

        var ms = new MemoryStream();
        stream.transferTo(ms.asOutputStream());

        var manifestData = ms.toByteArray();

        var depotManifest = DepotManifest.deserialize(manifestData);

        Assertions.assertTrue(depotManifest.getFilenamesEncrypted());
        Assertions.assertEquals(1195249848L, depotManifest.getEncryptedCRC());

        depotManifest.decryptFilenames(DEPOT_440_DECRYPTION_KEY);

        testDecryptedManifest(depotManifest);
    }

    @Test
    public void parsesAndDecryptsManifest() throws IOException, NoSuchAlgorithmException {
        var stream = getClass().getResourceAsStream("/depot/depot_440_1118032470228587934.manifest");

        var ms = new MemoryStream();
        stream.transferTo(ms.asOutputStream());

        var manifestData = ms.toByteArray();

        var depotManifest = DepotManifest.deserialize(manifestData);

        Assertions.assertTrue(depotManifest.getFilenamesEncrypted());
        Assertions.assertEquals(1606273976L, depotManifest.getEncryptedCRC());

        depotManifest.decryptFilenames(DEPOT_440_DECRYPTION_KEY);

        testDecryptedManifest(depotManifest);
    }

    @Test
    public void parsesDecryptedManifest() throws IOException, NoSuchAlgorithmException {
        var stream = getClass().getResourceAsStream("/depot/depot_440_1118032470228587934_decrypted.manifest");

        var ms = new MemoryStream();
        stream.transferTo(ms.asOutputStream());

        var manifestData = ms.toByteArray();

        var depotManifest = DepotManifest.deserialize(manifestData);

        testDecryptedManifest(depotManifest);
    }

    @Test
    public void roundtripSerializesManifestEncryptedManifest() throws IOException {
        var stream = getClass().getResourceAsStream("/depot/depot_440_1118032470228587934.manifest");

        var ms = new MemoryStream();
        stream.transferTo(ms.asOutputStream());

        var manifestData = ms.toByteArray();

        var depotManifest = DepotManifest.deserialize(manifestData);

        var actualStream = new MemoryStream();
        depotManifest.serialize(actualStream.asOutputStream());

        var actual = actualStream.toByteArray();

        // We are unable to write signatures, so validate everything except for the signature
        var signature = new byte[]{(byte) 0x17, (byte) 0xB8, (byte) 0x81, (byte) 0x1B};

        int actualOffset = indexOf(actual, signature); // DepotManifest.PROTOBUF_SIGNATURE_MAGIC
        int expectedOffset = indexOf(manifestData, signature);

        byte expectedByte = manifestData[1109];
        byte actualByte = actual[1109];
        System.out.println("At index 1109:");
        System.out.println("Expected byte: " + expectedByte);
        System.out.println("Actual byte: " + actualByte);

// Maybe also look at surrounding bytes for context
        System.out.println("Expected bytes around 1109:");
        for (int i = 1105; i < 1115; i++) {
            System.out.println("Index " + i + ": " + manifestData[i]);
        }
        System.out.println("Actual bytes around 1109:");
        for (int i = 1105; i < 1115; i++) {
            System.out.println("Index " + i + ": " + actual[i]);
        }

        Assertions.assertTrue(actualOffset > 0);
        Assertions.assertTrue(expectedOffset > 0);
        Assertions.assertArrayEquals(
                Arrays.copyOfRange(manifestData, 0, expectedOffset),
                Arrays.copyOfRange(actual, 0, actualOffset)
        );

        // We dont have `BitConverter.ToInt32`
        int expectedSignatureLength;
        try (var bais = new ByteArrayInputStream(manifestData);
             var br = new BinaryReader(bais)) {
            expectedSignatureLength = br.readBytes(expectedOffset + 4).length;
        }
        int actualSignatureLength;
        try (var bais = new ByteArrayInputStream(manifestData);
             var br = new BinaryReader(bais)) {
            actualSignatureLength = br.readBytes(expectedOffset + 4).length;
        }

        //int expectedSignatureLength = readInt32(manifestData, expectedOffset + 4);
        // int actualSignatureLength = readInt32(actual, actualOffset + 4);

        Assertions.assertEquals(131, expectedSignatureLength);
        Assertions.assertEquals(0, actualSignatureLength);
        Assertions.assertArrayEquals(
                Arrays.copyOfRange(manifestData, expectedOffset + expectedSignatureLength + 8, manifestData.length),
                Arrays.copyOfRange(actual, actualOffset + 8, actual.length)
        );
    }

    @Test
    public void roundtripSerializesManifestByteIdentical() throws IOException {
        var stream = getClass().getResourceAsStream("/depot/depot_440_1118032470228587934_decrypted.manifest");

        var ms = new MemoryStream();
        stream.transferTo(ms.asOutputStream());

        var manifestData = ms.toByteArray();

        var depotManifest = DepotManifest.deserialize(manifestData);

        var actualStream = new MemoryStream();
        depotManifest.serialize(actualStream.asOutputStream());

        var actual = actualStream.toByteArray();

        Assertions.assertArrayEquals(manifestData, actual);
    }

    private void testDecryptedManifest(DepotManifest depotManifest) throws NoSuchAlgorithmException {
        Assertions.assertFalse(depotManifest.getFilenamesEncrypted());
        Assertions.assertEquals(440L, depotManifest.getDepotID());
        Assertions.assertEquals(1118032470228587934L, depotManifest.getManifestGID());
        Assertions.assertEquals(825745L, depotManifest.getTotalUncompressedSize());
        Assertions.assertEquals(43168L, depotManifest.getTotalCompressedSize());
        Assertions.assertEquals(7, depotManifest.getFiles().size());
        Assertions.assertEquals(
                ZonedDateTime.of(2013, 4, 17, 20, 39, 24, 0, ZoneOffset.UTC).toInstant(),
                depotManifest.getCreationTime().toInstant()
        );

        Assertions.assertEquals(Path.of("bin", "dxsupport.cfg").toString(), depotManifest.getFiles().get(0).getFileName());
        Assertions.assertEquals(Path.of("bin", "dxsupport.csv").toString(), depotManifest.getFiles().get(1).getFileName());
        Assertions.assertEquals(Path.of("bin", "dxsupport_episodic.cfg").toString(), depotManifest.getFiles().get(2).getFileName());
        Assertions.assertEquals(Path.of("bin", "dxsupport_sp.cfg").toString(), depotManifest.getFiles().get(3).getFileName());
        Assertions.assertEquals(Path.of("bin", "vidcfg.bin").toString(), depotManifest.getFiles().get(4).getFileName());
        Assertions.assertEquals(Path.of("hl2", "media", "startupvids.txt").toString(), depotManifest.getFiles().get(5).getFileName());
        Assertions.assertEquals(Path.of("tf", "media", "startupvids.txt").toString(), depotManifest.getFiles().get(6).getFileName());

        Assertions.assertEquals(EDepotFileFlag.from(0), depotManifest.getFiles().get(0).getFlags());
        Assertions.assertEquals(398709L, depotManifest.getFiles().get(0).getTotalSize());
        Assertions.assertArrayEquals(
                Strings.decodeHex("BAC8E2657470B2EB70D6DDCD6C07004BE8738697"),
                depotManifest.getFiles().get(2).getFileHash()
        );

        for (var file : depotManifest.getFiles()) {
            Assertions.assertArrayEquals(
                    file.getFileNameHash(),
                    CryptoHelper.shaHash(file.getFileName().replace('/', '\\').getBytes())
            );
            Assertions.assertNotNull(file.getLinkTarget());
            Assertions.assertEquals(1, file.getChunks().size());
        }

        var chunk = depotManifest.getFiles().get(6).getChunks().get(0);
        Assertions.assertEquals(963249608L, chunk.getChecksum());
        Assertions.assertEquals(144L, chunk.getCompressedLength());
        Assertions.assertEquals(17L, chunk.getUncompressedLength());
        Assertions.assertEquals(0L, chunk.getOffset());
        Assertions.assertArrayEquals(
                Strings.decodeHex("94020BDE145A521EDEC9A9424E7A90FD042481E9"),
                chunk.getChunkID()
        );
    }

    // Java or Apache doesn't have a indexOf(byte[], byte[])
    // This is taken from guava since we don't have that lib as a dependency.
    private static int indexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return 0;
        }

        outer:
        for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static int readInt32(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
                ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) |
                ((data[offset + 3] & 0xFF) << 24);
    }
}
