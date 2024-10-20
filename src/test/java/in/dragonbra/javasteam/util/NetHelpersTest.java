package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class NetHelpersTest extends TestBase {

    private static final InetAddress loopbackV4 = InetAddress.getLoopbackAddress();
    private static final InetAddress loopbackV6;

    static {
        try {
            loopbackV6 = InetAddress.getByName("::1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getMsgIPAddress() {
        byte[] ipv6Bytes = {
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 1
        };

        Assertions.assertEquals(2130706433, NetHelpers.getMsgIPAddress(loopbackV4).getV4());
        Assertions.assertArrayEquals(ipv6Bytes, NetHelpers.getMsgIPAddress(loopbackV6).getV6().toByteArray());
    }

    @Test
    public void getIPAddressFromMsg() {
        var v4Addr = NetHelpers.getMsgIPAddress(loopbackV4);
        Assertions.assertEquals(loopbackV4, NetHelpers.getIPAddress(v4Addr));

        var v6Addr = NetHelpers.getMsgIPAddress(loopbackV6);
        Assertions.assertEquals(loopbackV6, NetHelpers.getIPAddress(v6Addr));
    }

    @Test
    public void getIPAddress() throws UnknownHostException {
        Assertions.assertEquals(loopbackV4, NetHelpers.getIPAddress(2130706433));
        Assertions.assertEquals(InetAddress.getByName("0.0.0.1"), NetHelpers.getIPAddress(1));
        Assertions.assertEquals(InetAddress.getByName("255.255.255.255"), NetHelpers.getIPAddress(0xFFFFFFFF));
        Assertions.assertEquals(InetAddress.getByName("0.0.0.0"), NetHelpers.getIPAddress(0));
    }

    @Test
    public void getIPAddressAsInt() throws UnknownHostException {
        Assertions.assertEquals(2130706433, NetHelpers.getIPAddress(loopbackV4));
        Assertions.assertEquals(1, NetHelpers.getIPAddress(InetAddress.getByName("0.0.0.1")));
        Assertions.assertEquals(0xFFFFFFFF, NetHelpers.getIPAddress(InetAddress.getByName("255.255.255.255")));
        Assertions.assertEquals(-1062731775, NetHelpers.getIPAddress(InetAddress.getByName("192.168.0.1")));
        Assertions.assertEquals(167772161, NetHelpers.getIPAddress(InetAddress.getByName("10.0.0.1")));
        Assertions.assertEquals(-1408237567, NetHelpers.getIPAddress(InetAddress.getByName("172.16.0.1")));
    }

    @Test
    public void obfuscatePrivateIP() {
        byte[] ipv6Bytes = {
                (byte) 0x0D, (byte) 0xF0, (byte) 0xAD, (byte) 0xBA,
                (byte) 0x0D, (byte) 0xF0, (byte) 0xAD, (byte) 0xBA,
                (byte) 0x0D, (byte) 0xF0, (byte) 0xAD, (byte) 0xBA,
                (byte) 0x0D, (byte) 0xF0, (byte) 0xAD, (byte) 0xBB
        };

        var v4Addr = NetHelpers.getMsgIPAddress(loopbackV4);
        Assertions.assertEquals(3316510732L, NetHelpers.obfuscatePrivateIP(v4Addr).getV4() & 0xFFFFFFFFL);
        var v6Addr = NetHelpers.getMsgIPAddress(loopbackV6);
        Assertions.assertArrayEquals(ipv6Bytes, NetHelpers.obfuscatePrivateIP(v6Addr).getV6().toByteArray());
    }

    @Test
    public void tryParseIPEndPoint() throws UnknownHostException {
        var v4Addr = NetHelpers.tryParseIPEndPoint("127.0.0.1:1337");
        Assertions.assertNotNull(v4Addr);
        Assertions.assertEquals(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 1337), v4Addr);

        var v6Addr = NetHelpers.tryParseIPEndPoint("[::1]:1337");
        Assertions.assertNotNull(v6Addr);
        Assertions.assertEquals(new InetSocketAddress(loopbackV6, 1337), v6Addr);

        var v6Addr2 = NetHelpers.tryParseIPEndPoint("::1:1337");
        Assertions.assertNotNull(v6Addr2);
        Assertions.assertEquals(new InetSocketAddress(loopbackV6, 1337), v6Addr2);
    }

    @Test
    public void fail_getIPAddressAsLong_If_V6() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> NetHelpers.getIPAddress(loopbackV6));
    }

    @Test
    public void testIntToAddress() throws UnknownHostException {
        int ipAddress = -1560361686;
        InetAddress address = NetHelpers.getIPAddress(ipAddress);
        Assertions.assertEquals(InetAddress.getByName("162.254.197.42"), address);
    }

    @Test
    public void testAddressToInt() throws UnknownHostException {
        int address = NetHelpers.getIPAddress(InetAddress.getByName("162.254.197.42"));
        Assertions.assertEquals(-1560361686, address);
    }
}
