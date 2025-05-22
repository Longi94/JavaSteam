package `in`.dragonbra.javasteam.steam.handlers.steamauthticket.callback

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.JobID

/**
 * This callback is fired when Steam accepts our auth ticket as valid.
 *
 * @author Lossy
 * @since 2025-05-22
 */
@Suppress("unused")
class TicketAcceptedCallback(
    jobID: JobID,
    body: SteammessagesClientserver.CMsgClientAuthListAck.Builder,
) : CallbackMsg() {
    /**
     * A [List] of AppIDs of the games that have generated tickets.
     */
    val appIDs: List<Int> = body.appIdsList

    /**
     * A [List] of CRC32 hashes of activated tickets.
     */
    val activeTicketsCRC: List<Int> = body.ticketCrcList

    /**
     * Number of message in sequence.
     */
    val messageSequence: Int = body.messageSequence

    init {
        this.jobID = jobID
    }
}
