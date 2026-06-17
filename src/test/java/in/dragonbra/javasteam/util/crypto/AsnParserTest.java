package in.dragonbra.javasteam.util.crypto;

import in.dragonbra.javasteam.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AsnParserTest extends TestBase {

    private static List<Byte> bytes(int... values) {
        Byte[] result = new Byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return Arrays.asList(result);
    }

    @Test
    public void positionTracking() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x01, 0x02, 0x03));
        assertEquals(0, parser.currentPosition());
        assertEquals(3, parser.remainingBytes());

        parser.getNextOctet();
        assertEquals(1, parser.currentPosition());
        assertEquals(2, parser.remainingBytes());
    }

    @Test
    public void getNextOctet() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x42));
        assertEquals(0x42, parser.getNextOctet());
        assertEquals(0, parser.remainingBytes());
    }

    @Test
    public void getNextOctetEmptyThrows() {
        var parser = new AsnParser(Collections.emptyList());
        assertThrows(BerDecodeException.class, parser::getNextOctet);
    }

    @Test
    public void getLengthShortForm() throws BerDecodeException {
        assertEquals(127, new AsnParser(bytes(0x7f)).getLength());
        assertEquals(0, new AsnParser(bytes(0x00)).getLength());
    }

    @Test
    public void getLengthLongForm() throws BerDecodeException {
        // 0x81 = long form, 1 byte follows: 0x42 = 66
        assertEquals(0x42, new AsnParser(bytes(0x81, 0x42)).getLength());
    }

    @Test
    public void getLengthInvalidEncodingThrows() {
        // 0x85 = long form with 5 length bytes — exceeds max of 4
        var parser = new AsnParser(bytes(0x85, 0x00, 0x00, 0x00, 0x00, 0x00));
        assertThrows(BerDecodeException.class, parser::getLength);
    }

    @Test
    public void next() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x02, 0x02, 0xAA, 0xBB));
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB}, parser.next());
    }

    @Test
    public void nextNull() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x05, 0x00));
        assertEquals(0, parser.nextNull());
        assertEquals(0, parser.remainingBytes());
    }

    @Test
    public void nextNullWrongTagThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x02, 0x00)).nextNull());
    }

    @Test
    public void nextNullNonZeroSizeThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x05, 0x01)).nextNull());
    }

    @Test
    public void isNextNull() {
        assertTrue(new AsnParser(bytes(0x05, 0x00)).isNextNull());
        assertFalse(new AsnParser(bytes(0x02, 0x00)).isNextNull());
    }

    @Test
    public void nextSequence() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x30, 0x03, 0x01, 0x02, 0x03));
        assertEquals(3, parser.nextSequence());
        assertEquals(3, parser.remainingBytes());
    }

    @Test
    public void nextSequenceWrongTagThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x02, 0x03, 0x01, 0x02, 0x03)).nextSequence());
    }

    @Test
    public void nextSequenceOverflowThrows() {
        // length claims 5 but only 1 byte remains
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x30, 0x05, 0x01)).nextSequence());
    }

    @Test
    public void isNextSequence() {
        assertTrue(new AsnParser(bytes(0x30, 0x00)).isNextSequence());
        assertFalse(new AsnParser(bytes(0x02, 0x00)).isNextSequence());
    }

    @Test
    public void nextOctetString() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x04, 0x02, 0xAA, 0xBB));
        assertEquals(2, parser.nextOctetString());
        assertEquals(2, parser.remainingBytes());
    }

    @Test
    public void nextOctetStringWrongTagThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x02, 0x02, 0xAA, 0xBB)).nextOctetString());
    }

    @Test
    public void isNextOctetString() {
        assertTrue(new AsnParser(bytes(0x04, 0x00)).isNextOctetString());
        assertFalse(new AsnParser(bytes(0x02, 0x00)).isNextOctetString());
    }

    @Test
    public void nextBitString() throws BerDecodeException {
        // tag 0x03, length 4, unused-bits byte 0x00, then 3 bytes content
        var parser = new AsnParser(bytes(0x03, 0x04, 0x00, 0xDE, 0xAD, 0xBE));
        assertEquals(3, parser.nextBitString());
        assertEquals(3, parser.remainingBytes());
    }

    @Test
    public void nextBitStringNonZeroUnusedThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x03, 0x02, 0x01, 0xDE)).nextBitString());
    }

    @Test
    public void isNextBitString() {
        assertTrue(new AsnParser(bytes(0x03, 0x00)).isNextBitString());
        assertFalse(new AsnParser(bytes(0x02, 0x00)).isNextBitString());
    }

    @Test
    public void nextInteger() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x02, 0x03, 0x01, 0x02, 0x03));
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, parser.nextInteger());
    }

    @Test
    public void nextIntegerWrongTagThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x05, 0x03, 0x01, 0x02, 0x03)).nextInteger());
    }

    @Test
    public void nextIntegerOverflowThrows() {
        // length claims 5 but only 1 byte remains
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x02, 0x05, 0x01)).nextInteger());
    }

    @Test
    public void isNextInteger() {
        assertTrue(new AsnParser(bytes(0x02, 0x01, 0x00)).isNextInteger());
        assertFalse(new AsnParser(bytes(0x05, 0x00)).isNextInteger());
    }

    @Test
    public void nextOID() throws BerDecodeException {
        var parser = new AsnParser(bytes(0x06, 0x03, 0x55, 0x04, 0x03));
        assertArrayEquals(new byte[]{0x55, 0x04, 0x03}, parser.nextOID());
    }

    @Test
    public void nextOIDWrongTagThrows() {
        assertThrows(BerDecodeException.class, () -> new AsnParser(bytes(0x02, 0x03, 0x55, 0x04, 0x03)).nextOID());
    }
}
