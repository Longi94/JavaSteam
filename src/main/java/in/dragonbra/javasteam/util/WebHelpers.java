package in.dragonbra.javasteam.util;

import java.nio.charset.StandardCharsets;

/**
 * @author lngtr
 * @since 2018-04-16
 */
public class WebHelpers {

    private static boolean isUrlSafeChar(char ch) {
        return ch >= 'a' && ch <= 'z' ||
                ch >= 'A' && ch <= 'Z' ||
                ch >= '0' && ch <= '9' ||
                ch == '-' ||
                ch == '.' ||
                ch == '_';
    }

    public static String urlEncode(String input) {
        return urlEncode(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String urlEncode(byte[] input) {
        StringBuilder encoded = new StringBuilder(input.length * 2);

        for (byte i : input) {
            char inch = (char) i;

            if (isUrlSafeChar(inch)) {
                encoded.append(inch);
            } else if (inch == ' ') {
                encoded.append('+');
            } else {
                encoded.append(String.format("%%%02X", i));
            }
        }

        return encoded.toString();
    }
}
