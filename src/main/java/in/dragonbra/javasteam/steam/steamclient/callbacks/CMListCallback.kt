package `in`.dragonbra.javasteam.steam.steamclient.callbacks

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientCMList
import `in`.dragonbra.javasteam.steam.discovery.ServerRecord
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.util.NetHelpers
import java.net.InetSocketAddress
import java.util.*

/**
 * This callback is received when the client has received the CM list from Steam.
 */
class CMListCallback(cmMsg: CMsgClientCMList.Builder) : CallbackMsg() {

    /**
     * Gets the CM server list.
     */
    val servers: List<ServerRecord>

    init {
        val cmList = cmMsg.cmAddressesList.zip(cmMsg.cmPortsList) { address, port ->
            ServerRecord.createSocketServer(InetSocketAddress(NetHelpers.getIPAddress(address), port))
        }

        val webSocketList = cmMsg.cmWebsocketAddressesList.map(ServerRecord::createWebSocketServer)

        servers = cmList + webSocketList
    }
}
