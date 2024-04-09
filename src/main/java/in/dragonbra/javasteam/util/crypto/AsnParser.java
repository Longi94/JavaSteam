package in.dragonbra.javasteam.util.crypto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-24
 */
@SuppressWarnings("unused")
class AsnParser {
    private final int initialCount;
    private final List<Byte> octets = new ArrayList<>();

    public AsnParser(Collection<Byte> values) {
        octets.addAll(values);
        initialCount = octets.size();
    }

    public int currentPosition() {
        return initialCount - octets.size();
    }

    public int remainingBytes() {
        return octets.size();
    }

    int getLength() throws BerDecodeException {
        int length = 0;

        // Checkpoint
        final int position = currentPosition();

        final byte b = getNextOctet();

        if (b == (b & 0x7f)) {
            return b;
        }
        int i = b & 0x7f;

        if (i > 4) {
            throw new BerDecodeException(String.format("Invalid Length Encoding. Length uses %d _octets", i), position);
        }

        while (0 != i--) {
            // shift left
            length <<= 8;

            length |= getNextOctet();
        }

        return length & 0xFF;
    }

    public byte[] next() throws BerDecodeException {
        int position = currentPosition();

        byte b = getNextOctet();

        final int length = getLength();
        if (length > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Size. Specified: %d, Remaining: %d", length, remainingBytes()), position);
        }

        return getOctets(length);
    }

    public byte getNextOctet() throws BerDecodeException {
        int position = currentPosition();

        if (0 == remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect size. Specified: %d, Remaining: %d", 1, remainingBytes()), position);
        }

        return getOctets(1)[0];
    }

    private byte[] getOctets(int octetCount) throws BerDecodeException {
        int position = currentPosition();

        if (octetCount > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect size. Specified: %d, Remaining: %d", octetCount, remainingBytes()), position);
        }

        byte[] values = new byte[octetCount];

        for (int i = 0; i < octetCount; i++) {
            values[i] = octets.remove(0);
        }

        return values;
    }

    public boolean isNextNull() {
        return 0x05 == octets.get(0);
    }

    public int nextNull() throws BerDecodeException {
        final int position = currentPosition();

        byte b = getNextOctet();
        if (0x05 != b) {
            throw new BerDecodeException(String.format("Expected Null. Specified Identifier: %d", b), position);
        }

        // Next octet must be 0
        b = getNextOctet();
        if (0x00 != b) {
            throw new BerDecodeException(String.format("Null has non-zero size. Size: %d", b), position);
        }

        return 0;
    }

    public boolean isNextSequence() {
        return 0x30 == octets.get(0);
    }

    public int nextSequence() throws BerDecodeException {
        final int position = currentPosition();

        final byte b = getNextOctet();
        if (0x30 != b) {
            throw new BerDecodeException(String.format("Expected Sequence. Specified Identifier: %d", b), position);
        }

        final int length = getLength();
        if (length > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Sequence Size. Specified: %d, Remaining: %d", length, remainingBytes()), position);
        }

        return length;
    }

    public boolean isNextOctetString() {
        return 0x04 == octets.get(0);
    }

    public int nextOctetString() throws BerDecodeException {
        final int position = currentPosition();

        final byte b = getNextOctet();
        if (0x04 != b) {
            throw new BerDecodeException(String.format("Expected Octet String.Specified Identifier: %d", b), position);
        }

        final int length = getLength();
        if (length > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Octet String Size. Specified: %d, Remaining: %d", length, remainingBytes()), position);
        }

        return length;
    }

    public boolean isNextBitString() {
        return 0x03 == octets.get(0);
    }

    public int nextBitString() throws BerDecodeException {
        final int position = currentPosition();

        byte b = getNextOctet();
        if (0x03 != b) {
            throw new BerDecodeException(String.format("Expected Bit String. Specified Identifier: %d", b), position);
        }

        int length = getLength();

        // We need to consume unused bits, which is the first octet of the remaining values
        b = octets.get(0);
        octets.remove(0);
        length--;

        if (0x00 != b) {
            throw new BerDecodeException("The first octet of BitString must be 0", position);
        }

        return length;
    }

    public boolean isNextInteger() {
        return 0x02 == octets.get(0);
    }

    public byte[] nextInteger() throws BerDecodeException {
        final int position = currentPosition();

        final byte b = getNextOctet();
        if (0x02 != b) {
            throw new BerDecodeException(String.format("Expected Integer. Specified Identifier: %d", b), position);
        }

        final int length = getLength();
        if (length > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Integer Size. Specified: %d, Remaining: %d", length, remainingBytes()), position);
        }

        return getOctets(length);
    }

    public byte[] nextOID() throws BerDecodeException {
        final int position = currentPosition();

        final byte b = getNextOctet();
        if (0x06 != b) {
            throw new BerDecodeException(String.format("Expected Object Identifier. Specified Identifier: %d", b), position);
        }

        final int length = getLength();
        if (length > remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Object Identifier Size. Specified: %d, Remaining: %d", length, remainingBytes()), position);
        }

        final byte[] values = new byte[length];

        for (int i = 0; i < length; i++) {
            values[i] = octets.remove(0);
        }

        return values;
    }
}
