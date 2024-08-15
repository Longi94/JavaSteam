package `in`.dragonbra.javasteam.steam.handlers.steammasterserver.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGMSClientServerQueryResponse
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.Server
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamMasterServer.serverQuery].
 */
class QueryCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * @return the list of servers
     */
    val servers: List<Server>

    init {
        val queryResponse = ClientMsgProtobuf<CMsgGMSClientServerQueryResponse.Builder>(
            CMsgGMSClientServerQueryResponse::class.java,
            packetMsg
        )
        val msg = queryResponse.body

        jobID = queryResponse.targetJobID

        servers = msg.serversList.map { Server(it) }
    }
}
