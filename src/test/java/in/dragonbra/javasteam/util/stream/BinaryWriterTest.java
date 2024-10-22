package in.dragonbra.javasteam.util.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BinaryWriterTest {

    private MemoryStream memoryStream;
    private BinaryWriter binaryWriter;

    @BeforeEach
    void setUp() {
        memoryStream = new MemoryStream();
        binaryWriter = new BinaryWriter(memoryStream.asOutputStream());
    }

    @Test
    void testWriteInt() throws IOException {
        binaryWriter.writeInt(1); // 1 should be written as [01 00 00 00]

        byte[] expected = {0x01, 0x00, 0x00, 0x00};

        Assertions.assertArrayEquals(expected, memoryStream.toByteArray());
    }

    @Test
    void testWriteShort() throws IOException {
        binaryWriter.writeShort((short) 1); // 1 should be written as [01 00]

        byte[] expected = {0x01, 0x00};

        Assertions.assertArrayEquals(expected, memoryStream.toByteArray());
    }

    @Test
    void testWriteLong() throws IOException {
        binaryWriter.writeLong(1L); // 1 should be written as [01 00 00 00 00 00 00 00]

        byte[] expected = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        Assertions.assertArrayEquals(expected, memoryStream.toByteArray());
    }

    @Test
    void testWriteFloat() throws IOException {
        binaryWriter.writeFloat(3.14f);

        var ms = new MemoryStream(memoryStream.toByteArray());
        var br = new BinaryReader(ms);

        Assertions.assertEquals(3.14f, br.readFloat());
    }

    @Test
    void testWriteDouble() throws IOException {
        binaryWriter.writeDouble(3.14159);

        var ms = new MemoryStream(memoryStream.toByteArray());
        var br = new BinaryReader(ms);

        Assertions.assertEquals(3.14159, br.readDouble(), 0.001);
    }

    @Test
    void testWriteBoolean() throws IOException {
        binaryWriter.writeBoolean(true);

        Assertions.assertArrayEquals(new byte[]{1}, memoryStream.toByteArray());

        memoryStream.reset(); // Reset stream for next test
        binaryWriter.writeBoolean(false);

        Assertions.assertArrayEquals(new byte[]{0}, memoryStream.toByteArray());
    }

    @Test
    void testWriteByte() throws IOException {
        binaryWriter.writeByte((byte) 0xAB);

        Assertions.assertArrayEquals(new byte[]{(byte) 0xAB}, memoryStream.toByteArray());
    }

    @Test
    void testWriteChar() throws IOException {
        binaryWriter.writeChar('A'); // ASCII value of 'A' is 65

        Assertions.assertArrayEquals(new byte[]{65}, memoryStream.toByteArray());
    }
}
