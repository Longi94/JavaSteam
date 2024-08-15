package `in`.dragonbra.javasteam.steam.handlers.steammasterserver

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGMSClientServerQueryResponse
import `in`.dragonbra.javasteam.util.NetHelpers
import java.net.InetSocketAddress

/**
 * Represents a single server.
 */
@Suppress("unused")
class Server(server: CMsgGMSClientServerQueryResponse.Server) {

    /**
     * Gets the IP endpoint of the server.
     */
    val endPoint: InetSocketAddress = InetSocketAddress(NetHelpers.getIPAddress(server.serverIp.v4), server.queryPort)

    /**
     * Gets the number of Steam authenticated players on this server
     */
    val authedPlayers: Int = server.authPlayers
}
