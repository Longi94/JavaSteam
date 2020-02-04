package in.dragonbra.javasteam.steam.handlers.steammasterserver;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGMSClientServerQueryResponse;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;

/**
 * Represents a single server.
 */
public class Server {

    private InetSocketAddress endPoint;

    private int authedPlayers;

    //NOTE: getDeprecatedServerIp() added
    public Server(CMsgGMSClientServerQueryResponse.Server server) {
        endPoint = new InetSocketAddress(NetHelpers.getIPAddress(server.getDeprecatedServerIp()), server.getServerPort());
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
