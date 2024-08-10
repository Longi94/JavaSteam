package in.dragonbra.javasteam.steam.handlers.steammasterserver;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGMSClientServerQueryResponse;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;

/**
 * Represents a single server.
 */
public class Server {

    private final InetSocketAddress endPoint;

    private final int authedPlayers;

    public Server(CMsgGMSClientServerQueryResponse.Server server) {
        endPoint = new InetSocketAddress(NetHelpers.getIPAddress(server.getServerIp().getV4()), server.getQueryPort());
        authedPlayers = server.getAuthPlayers();
    }

    /**
     * @return the IP endpoint of the server
     */
    public InetSocketAddress getEndPoint() {
        return endPoint;
    }

    /**
     * @return the number of Steam authenticated players on this server
     */
    public int getAuthedPlayers() {
        return authedPlayers;
    }
}
