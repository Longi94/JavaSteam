package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.NameTableInstance
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * Callback fired in response to calling [SteamFriends.requestAliasHistory].
 */
class AliasHistoryCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the responses to the steam ids
     */
    val responses: List<NameTableInstance>

    init {
        val resp = ClientMsgProtobuf<CMsgClientAMGetPersonaNameHistoryResponse.Builder>(
            CMsgClientAMGetPersonaNameHistoryResponse::class.java,
            packetMsg
        )
        jobID = resp.targetJobID

        responses = resp.body.responsesList.map { NameTableInstance(it) }
    }
}
