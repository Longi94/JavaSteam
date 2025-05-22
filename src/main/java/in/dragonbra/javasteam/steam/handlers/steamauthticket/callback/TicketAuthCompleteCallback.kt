package `in`.dragonbra.javasteam.steam.handlers.steamauthticket.callback

import `in`.dragonbra.javasteam.enums.EAuthSessionResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired when generated ticket was successfully used to authenticate user.
 *
 * @author Lossy
 * @since 2025-05-22
 */
@Suppress("unused")
class TicketAuthCompleteCallback(
    targetJobID: JobID,
    body: SteammessagesClientserver.CMsgClientTicketAuthComplete.Builder,
) : CallbackMsg() {
    /**
     * Steam response to authentication request.
     */
    val authSessionResponse: EAuthSessionResponse = EAuthSessionResponse.from(body.eauthSessionResponse)

    /**
     * Authentication state.
     */
    val state: Int = body.estate

    /**
     * [GameID] of the game the token was generated for.
     */
    val gameID: GameID = GameID(body.gameId)

    /**
     * [SteamID] of the game owner.
     */
    val ownerSteamID: SteamID = SteamID(body.ownerSteamId)

    /**
     * [SteamID] of the game server.
     */
    val steamID: SteamID = SteamID(body.steamId)

    /**
     * CRC of the ticket.
     */
    val ticketCRC: Int = body.ticketCrc

    /**
     * Sequence of the ticket.
     */
    val ticketSequence: Int = body.ticketSequence

    init {
        this.jobID = targetJobID
    }
}
