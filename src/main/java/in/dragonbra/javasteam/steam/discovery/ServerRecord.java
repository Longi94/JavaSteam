package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;
import java.util.EnumSet;

/**
 * Represents the information needed to connect to a CM server
 */
public class ServerRecord {

    private InetSocketAddress endpoint;

    private EnumSet<ProtocolTypes> protocolTypes;

    ServerRecord(InetSocketAddress endpoint, ProtocolTypes protocolTypes) {
        this(endpoint, EnumSet.of(protocolTypes));
    }

    private ServerRecord(InetSocketAddress endpoint, EnumSet<ProtocolTypes> protocolTypes) {
        if (endpoint == null) {
            throw new IllegalArgumentException("endpoint is null");
        }

        this.endpoint = endpoint;
        this.protocolTypes = protocolTypes;
    }

    public String getHost() {
        return this.endpoint.getHostString();
    }

    public int getPort() {
        return endpoint.getPort();
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public EnumSet<ProtocolTypes> getProtocolTypes() {
        return protocolTypes;
    }

    public static ServerRecord createServer(String host, int port, ProtocolTypes protocolTypes) {
        return createServer(host, port, EnumSet.of(protocolTypes));
    }

    public static ServerRecord createServer(String host, int port, EnumSet<ProtocolTypes> protocolTypes) {
        return new ServerRecord(new InetSocketAddress(host, port), protocolTypes);
    }

    public static ServerRecord createSocketServer(InetSocketAddress endpoint) {
        return new ServerRecord(endpoint, EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
    }

    /**
     * Creates a Socket server given an IP endpoint.
     *
     * @param address The IP address and port of the server, as a string.
     * @return A new [ServerRecord], if the address was able to be parsed. **null** otherwise.
     */
    public static ServerRecord tryCreateSocketServer(String address) {
        InetSocketAddress endpoint;

        endpoint = NetHelpers.tryParseIPEndPoint(address);

        if (endpoint == null) {
            return null;
        }

        return new ServerRecord(endpoint, EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP));
    }

    public static ServerRecord createWebSocketServer(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address is null");
        }

        final int defaultPort = 443;

        String[] split = address.split(":");

        InetSocketAddress endpoint;
        if (split.length > 1) {
            endpoint = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
        } else {
            endpoint = new InetSocketAddress(address, defaultPort);
        }

        return new ServerRecord(endpoint, ProtocolTypes.WEB_SOCKET);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServerRecord)) {
            return false;
        }

        ServerRecord o = (ServerRecord) obj;

        return endpoint.equals(o.endpoint) && protocolTypes.equals(o.protocolTypes);
    }

    @Override
    public int hashCode() {
        return endpoint.hashCode() ^ protocolTypes.hashCode();
    }
}
