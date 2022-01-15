package in.dragonbra.javasteam.util;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * @author lngtr
 * @since 2018-02-22
 */
public class NetHelpers {

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

        if (!InetAddressValidator.getInstance().isValidInet4Address(split[0])) {
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
