package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;

import java.net.InetSocketAddress;

/**
 * Represents the information needed to connect to a CM server
 */
public class ServerRecord {

    private InetSocketAddress endpoint;

    private ProtocolTypes protocolTypes;

    ServerRecord(InetSocketAddress endpoint, ProtocolTypes protocolTypes) {
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

    public ProtocolTypes getProtocolTypes() {
        return protocolTypes;
    }

    public static ServerRecord createServer(String host, int port, ProtocolTypes protocolTypes) {
        return new ServerRecord(new InetSocketAddress(host, port), protocolTypes);
    }

    public static ServerRecord createSocketServer(InetSocketAddress endpoint) {
        return new ServerRecord(endpoint, ProtocolTypes.TCP_UDP);
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
}
