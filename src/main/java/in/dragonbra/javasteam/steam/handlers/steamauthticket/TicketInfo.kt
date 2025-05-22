package `in`.dragonbra.javasteam.steam.handlers.steamauthticket

import `in`.dragonbra.javasteam.util.Utils
import java.io.Closeable

/**
 * Represents a valid authorized session ticket.
 * @param handler The [SteamAuthTicket] handler.
 * @param appID Application the ticket was generated for.
 * @param ticket Bytes of the valid Session Ticket.
 *
 * @author Lossy
 * @since 2025-05-22
 */
class TicketInfo internal constructor(
    private val handler: SteamAuthTicket,
    internal val appID: Int,
    val ticket: ByteArray,
) : Closeable {

    /**
     * [No kDoc]
     */
    internal val ticketCRC: Long = Utils.crc32(ticket)

    /**
     * Discards the ticket.
     */
    override fun close() {
        handler.cancelAuthTicket(this)
    }
}
