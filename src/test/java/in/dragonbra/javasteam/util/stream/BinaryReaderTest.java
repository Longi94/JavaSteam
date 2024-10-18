package in.dragonbra.javasteam.util.stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BinaryReaderTest {

    private BinaryReader binaryReader;

    @Test
    void testReadInt() throws IOException {
        byte[] data = {1, 0, 0, 0}; // 1 in little-endian

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readInt();

        Assertions.assertEquals(1, result);
        Assertions.assertEquals(4, binaryReader.getPosition());
    }

    @Test
    void testReadBytes() throws IOException {
        byte[] data = {0x01, 0x02, 0x03, 0x04};

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readBytes(4);

        Assertions.assertArrayEquals(data, result);
        Assertions.assertEquals(4, binaryReader.getPosition());
    }

    @Test
    void testReadShort() throws IOException {
        byte[] data = {0x01, 0x00}; // 1 in little-endian

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readShort();

        Assertions.assertEquals(1, result);
        Assertions.assertEquals(2, binaryReader.getPosition());
    }

    @Test
    void testReadLong() throws IOException {
        byte[] data = {1, 0, 0, 0, 0, 0, 0, 0}; // 1 in little-endian

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readLong();

        Assertions.assertEquals(1L, result);
        Assertions.assertEquals(8, binaryReader.getPosition());
    }

    @Test
    void testReadChar() throws IOException {
        byte[] data = {65}; // ASCII 'A'

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readChar();

        Assertions.assertEquals('A', result);
        Assertions.assertEquals(1, binaryReader.getPosition());
    }

    @Test
    void testReadBoolean() throws IOException {
        byte[] data = {1}; // true

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readBoolean();

        Assertions.assertTrue(result);
        Assertions.assertEquals(1, binaryReader.getPosition());
    }

    @Test
    void testReadNullTermUtf8String() throws IOException {
        var string = "Hello\0";
        var data = string.getBytes(StandardCharsets.UTF_8);

        binaryReader = new BinaryReader(new MemoryStream(data));

        Assertions.assertEquals("Hello", binaryReader.readNullTermString());
        Assertions.assertEquals(6, binaryReader.getPosition());
    }

    @Test
    void testReadNullTermUtf8StringWithSpaces() throws IOException {
        var string = "Hello World With Spaces\0";
        var data = string.getBytes(StandardCharsets.UTF_8);

        binaryReader = new BinaryReader(new MemoryStream(data));

        Assertions.assertEquals("Hello World With Spaces", binaryReader.readNullTermString());
        Assertions.assertEquals(24, binaryReader.getPosition());
    }

    @Test
    void testReadFloat() throws IOException {
        int floatAsInt = Float.floatToIntBits(3.14f);
        byte[] data = {
                (byte) (floatAsInt & 0xff),
                (byte) ((floatAsInt >> 8) & 0xff),
                (byte) ((floatAsInt >> 16) & 0xff),
                (byte) ((floatAsInt >> 24) & 0xff)
        };

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readFloat();

        Assertions.assertEquals(3.14f, result, 0.001);
        Assertions.assertEquals(4, binaryReader.getPosition());
    }

    @Test
    void testReadDouble() throws IOException {
        var doubleAsLong = Double.doubleToLongBits(3.14159);
        byte[] data = {
                (byte) (doubleAsLong & 0xff),
                (byte) ((doubleAsLong >> 8) & 0xff),
                (byte) ((doubleAsLong >> 16) & 0xff),
                (byte) ((doubleAsLong >> 24) & 0xff),
                (byte) ((doubleAsLong >> 32) & 0xff),
                (byte) ((doubleAsLong >> 40) & 0xff),
                (byte) ((doubleAsLong >> 48) & 0xff),
                (byte) ((doubleAsLong >> 56) & 0xff)
        };

        binaryReader = new BinaryReader(new MemoryStream(data));

        var result = binaryReader.readDouble();

        Assertions.assertEquals(3.14159, result, 0.00001);
        Assertions.assertEquals(8, binaryReader.getPosition());
    }

    @Test
    void testEOFException() {
        byte[] data = {};
        binaryReader = new BinaryReader(new MemoryStream(data));

        Assertions.assertThrows(EOFException.class, () -> binaryReader.readBoolean());
        Assertions.assertThrows(EOFException.class, () -> binaryReader.readByte());
        Assertions.assertThrows(EOFException.class, () -> binaryReader.readChar());
        Assertions.assertThrows(EOFException.class, () -> binaryReader.readInt());
        Assertions.assertThrows(EOFException.class, () -> binaryReader.readNullTermString());
        Assertions.assertThrows(EOFException.class, () -> binaryReader.readShort());
    }
}
