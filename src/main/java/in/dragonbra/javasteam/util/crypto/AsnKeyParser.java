package in.dragonbra.javasteam.util.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class AsnKeyParser {
    private static final Logger logger = LogManager.getLogger(AsnKeyParser.class);

    private final AsnParser _parser;

    public AsnKeyParser(List<Byte> contents) {
        _parser = new AsnParser(contents);
    }

    public static byte[] trimLeadingZero(byte[] values) {
        byte[] r;
        if (0x00 == values[0] && values.length > 1) {
            r = new byte[values.length - 1];
            System.arraycopy(values, 1, r, 0, values.length - 1);
        } else {
            r = new byte[values.length];
            System.arraycopy(values, 0, r, 0, values.length);
        }

        return r;
    }

    public static boolean equalOid(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }

        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }

        return true;
    }

    public BigInteger[] parseRSAPublicKey() throws BerDecodeException {
        final BigInteger[] parameters = new BigInteger[2];

        // Current value

        // Sanity Check

        // Checkpoint
        int position = _parser.currentPosition();

        // Ignore Sequence - PublicKeyInfo
        int length = _parser.nextSequence();
        if (length != _parser.remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect Sequence Size. Specified: %d, Remaining: %d", length, _parser.remainingBytes()), position);
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore Sequence - AlgorithmIdentifier
        length = _parser.nextSequence();
        if (length > _parser.remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect AlgorithmIdentifier Size. Specified: %d, Remaining: %d", length, _parser.remainingBytes()), position);
        }

        // Checkpoint
        position = _parser.currentPosition();
        // Grab the OID
        final byte[] value = _parser.nextOID();
        final byte[] oid = {(byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d, (byte) 0x01, (byte) 0x01, (byte) 0x01};
        if (!equalOid(value, oid)) {
            throw new BerDecodeException("Expected OID 1.2.840.113549.1.1.1", position);
        }

        // Optional Parameters
        if (_parser.isNextNull()) {
            _parser.nextNull();
            // Also OK: value = _parser.Next();
        } else {
            // Gracefully skip the optional data
            _parser.next();
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore BitString - PublicKey
        length = _parser.nextBitString();
        if (length > _parser.remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect PublicKey Size. Specified: %d, Remaining: %d", length, _parser.remainingBytes()), position);
        }

        // Checkpoint
        position = _parser.currentPosition();

        // Ignore Sequence - RSAPublicKey
        length = _parser.nextSequence();
        if (length < _parser.remainingBytes()) {
            throw new BerDecodeException(String.format("Incorrect RSAPublicKey Size. Specified: %d, Remaining: %d", length, _parser.remainingBytes()), position);
        }

        parameters[0] = new BigInteger(1, trimLeadingZero(_parser.nextInteger()));
        parameters[1] = new BigInteger(1, trimLeadingZero(_parser.nextInteger()));

        assert 0 == _parser.remainingBytes();

        return parameters;
    }
}
