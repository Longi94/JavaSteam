package in.dragonbra.javasteam.util;

import java.math.BigInteger;

/**
 * @author lngtr
 * @since 2018-02-19
 */
public class Strings {

    /**
     * the constant 2^64
     */
    private static final BigInteger TWO_64 = BigInteger.ONE.shiftLeft(64);

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public String asUnsignedDecimalString(long l) {
        BigInteger b = BigInteger.valueOf(l);
        if (b.signum() < 0) {
            b = b.add(TWO_64);
        }
        return b.toString();
    }
}
