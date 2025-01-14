package `in`.dragonbra.javasteam.steam.handlers.steamgameserver.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EAuthSessionResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientTicketAuthComplete
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired when ticket authentication has completed.
 */
@Suppress("MemberVisibilityCanBePrivate")
class TicketAuthCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * @return the SteamID the ticket auth completed for
     */
    val steamID: SteamID

    /**
     * @return the GameID the ticket was for
     */
    val gameID: GameID

    /**
     * @return the authentication state
     */
    val state: Int

    /**
     * @return the auth session response
     */
    val authSessionResponse: EAuthSessionResponse?

    /**
     * @return the ticket CRC
     */
    val ticketCrc: Int

    /**
     * @return the ticket sequence
     */
    val ticketSequence: Int

    init {
        val authComplete = ClientMsgProtobuf<CMsgClientTicketAuthComplete.Builder>(
            CMsgClientTicketAuthComplete::class.java,
            packetMsg
        )
        val tickAuth = authComplete.body

        steamID = SteamID(tickAuth.steamId)
        gameID = GameID(tickAuth.gameId)

        state = tickAuth.estate

        authSessionResponse = EAuthSessionResponse.from(tickAuth.eauthSessionResponse)

        ticketCrc = tickAuth.ticketCrc
        ticketSequence = tickAuth.ticketSequence
    }
}
