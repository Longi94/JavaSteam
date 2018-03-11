package in.dragonbra.javasteam.util;

import org.apache.commons.lang3.ArrayUtils;

import java.net.InetAddress;
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

        ArrayUtils.reverse(result);

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
}
