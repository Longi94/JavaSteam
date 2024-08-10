package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGetAppOwnershipTicketResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamApps.getAppOwnershipTicket]
 */
class AppOwnershipTicketCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of requesting the ticket.
     */
    val result: EResult

    /**
     * Gets the AppID this ticket is for.
     */
    val appID: Int

    /**
     * Gets the ticket data.
     */
    val ticket: ByteArray

    init {
        val ticketResponse = ClientMsgProtobuf<CMsgClientGetAppOwnershipTicketResponse.Builder>(
            CMsgClientGetAppOwnershipTicketResponse::class.java,
            packetMsg
        )
        val msg = ticketResponse.body

        jobID = ticketResponse.targetJobID

        result = EResult.from(msg.eresult)
        appID = msg.appId
        ticket = msg.ticket.toByteArray()
    }
}
