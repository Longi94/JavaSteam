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

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] decodeHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
