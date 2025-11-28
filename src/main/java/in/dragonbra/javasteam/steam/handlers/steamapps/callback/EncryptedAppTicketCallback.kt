package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.EncryptedAppTicketOuterClass.EncryptedAppTicket
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientRequestEncryptedAppTicketResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamApps.requestEncryptedAppTicket]
 */
class EncryptedAppTicketCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of requesting the encrypted app ticket.
     */
    val result: EResult

    /**
     * Gets the AppID this ticket is for.
     */
    val appID: Int

    /**
     * Gets the encrypted app ticket, or null if the request failed.
     */
    val encryptedAppTicket: EncryptedAppTicket?

    init {
        val ticketResponse = ClientMsgProtobuf<CMsgClientRequestEncryptedAppTicketResponse.Builder>(
            CMsgClientRequestEncryptedAppTicketResponse::class.java,
            packetMsg
        )
        val msg = ticketResponse.body

        jobID = ticketResponse.targetJobID

        result = EResult.from(msg.eresult)
        appID = msg.appId
        encryptedAppTicket = if (msg.hasEncryptedAppTicket()) {
            msg.encryptedAppTicket
        } else {
            null
        }
    }
}
