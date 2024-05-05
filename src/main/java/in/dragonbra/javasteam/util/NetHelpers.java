package in.dragonbra.javasteam.util;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lngtr
 * @since 2018-02-22
 */
public class NetHelpers {

    private static final String IPV4_REGEX = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

    private static final Pattern pattern = Pattern.compile(IPV4_REGEX);

    public static InetAddress getIPAddress(int ipAddr) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(ipAddr);

        byte[] result = b.array();

        try {
            return InetAddress.getByAddress(result);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static int getIPAddress(InetAddress ip) {
        final ByteBuffer buff = ByteBuffer.wrap(ip.getAddress());
        return (int) (buff.getInt() & 0xFFFFFFFFL);
    }


    public static InetSocketAddress tryParseIPEndPoint(String address) {
        if (address == null) {
            return null;
        }

        String[] split = address.split(":");

        Matcher matcher = pattern.matcher(split[0]);

        if (!matcher.matches()) {
            return null;
        }

        try {
            if (split.length > 1) {
                return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
            }
        } catch (IllegalArgumentException exception) {
            // no-op
        }

        return null;
    }
}
