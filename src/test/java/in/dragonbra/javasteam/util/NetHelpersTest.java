package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.TestBase;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetHelpersTest extends TestBase {

    @Test
    public void testIntToAddress() throws UnknownHostException {
        int ipAddress = -1560361686;
        InetAddress address = NetHelpers.getIPAddress(ipAddress);
        assertEquals(InetAddress.getByName("162.254.197.42"), address);
    }

    @Test
    public void testAddressToInt() throws UnknownHostException {
        int address = NetHelpers.getIPAddress(InetAddress.getByName("162.254.197.42"));
        assertEquals(-1560361686, address);
    }
}