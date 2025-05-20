package in.dragonbra.javasteam.steam.cdn;

import in.dragonbra.javasteam.types.ChunkData;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DepotChunkTest {

    @Test
    public void decryptsAndDecompressesDepotChunkPKZip() throws IOException, NoSuchAlgorithmException {
        var stream = getClass().getClassLoader()
                .getResourceAsStream("depot/depot_440_chunk_bac8e2657470b2eb70d6ddcd6c07004be8738697.bin");

        var ms = new MemoryStream();
        IOUtils.copy(stream, ms.asOutputStream());

        var chunkData = ms.toByteArray();

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
        var stream = getClass().getClassLoader()
                .getResourceAsStream("depot/depot_232250_chunk_7b8567d9b3c09295cdbf4978c32b348d8e76c750.bin");

        var ms = new MemoryStream();
        IOUtils.copy(stream, ms.asOutputStream());

        var chunkData = ms.toByteArray();

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
        var stream = getClass().getClassLoader()
                .getResourceAsStream("depot/depot_3441461_chunk_9e72678e305540630a665b93e1463bc3983eb55a.bin");

        var ms = new MemoryStream();
        IOUtils.copy(stream, ms.asOutputStream());

        var chunkData = ms.toByteArray();

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
}
