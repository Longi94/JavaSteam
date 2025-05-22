package `in`.dragonbra.javasteam.steam.handlers.steamauthticket

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAuthList
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAuthListAck
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGameConnectTokens
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientTicketAuthComplete
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamauthticket.callback.TicketAcceptedCallback
import `in`.dragonbra.javasteam.steam.handlers.steamauthticket.callback.TicketAuthCompleteCallback
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.util.Utils
import `in`.dragonbra.javasteam.util.stream.BinaryWriter
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import kotlinx.coroutines.future.future
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * This handler generates auth session ticket and handles it's verification by steam.
 *
 * @constructor Initializes all necessary callbacks.
 *
 * @author Lossy
 * @since 2025-05-22
 */
@Suppress("unused")
class SteamAuthTicket : ClientMsgHandler() {

    companion object {
        private val sequence: AtomicInteger = AtomicInteger(0)

        // According to https://partner.steamgames.com/doc/api/ISteamUser#GetTicketForWebApiResponse_t
        //  the m_rgubTicket size is 2560 bytes
        private const val WEB_API_TICKET_SIZE: Int = 2560
    }

    /**
     * Represents the information about the generated authentication session ticket.
     */
    enum class TicketType(val value: Int) {
        /**
         * Default auth session ticket type.
         */
        AuthSession(2),

        /**
         * Web API auth session ticket type.
         */
        WebApiTicket(5),
    }

    private val dispatchMap = mapOf(
        EMsg.ClientAuthListAck to ::handleTicketAcknowledged,
        EMsg.ClientTicketAuthComplete to ::handleTicketAuthComplete,
        EMsg.ClientGameConnectTokens to ::handleGameConnectTokens,
        EMsg.ClientLogOff to ::handleLogOffResponse
    )

    private val gameConnectTokens = LinkedBlockingQueue<ByteArray>()

    private val ticketsByGame = ConcurrentHashMap<Int, MutableList<SteammessagesBase.CMsgAuthTicket>>()

    private val ticketChangeLock = Any()

    /**
     * Performs [session ticket](https://partner.steamgames.com/doc/api/ISteamUser#GetAuthSessionTicket)
     *  generation and validation for specified [appid].
     * @param appid Game to generate ticket for.
     * @return A task representing the asynchronous operation. The task result contains a <see cref="TicketInfo"/>
     *  object that provides details about the generated valid authentication session ticket.
     */
    fun getAuthSessionTicket(appid: Int): CompletableFuture<TicketInfo> = client.defaultScope.future {
        return@future getAuthSessionTicketInternal(appid, TicketType.AuthSession, "")
    }

    /**
     * Performs [WebApi session ticket](https://partner.steamgames.com/doc/api/ISteamUser#GetAuthTicketForWebApi)
     *  generation and validation for specified [appid] and [identity].
     * @param appid Game to generate ticket for.
     * @param identity The identity of the remote service that will authenticate the ticket. The service should provide a string identifier.
     * @return A task representing the asynchronous operation. The task result contains a [TicketInfo]
     *  object that provides details about the generated valid authentication WebApi session ticket.
     */
    fun getAuthTicketForWebApi(
        appid: Int,
        identity: String,
    ): CompletableFuture<TicketInfo> = client.defaultScope.future {
        return@future getAuthSessionTicketInternal(appid, TicketType.WebApiTicket, identity)
    }

    internal suspend fun getAuthSessionTicketInternal(
        appid: Int,
        ticketType: TicketType,
        identity: String?,
    ): TicketInfo {
        requireNotNull(client.cellID) { "User not logged in." }

        val apps = client.getHandler<SteamApps>()

        requireNotNull(apps) { "Steam Apps instance was null." }

        val appTicket = apps.getAppOwnershipTicket(appid).await()

        if (appTicket.result != EResult.OK) {
            throw Exception(
                "Failed to obtain app ownership ticket. Result: ${appTicket.result}. " +
                    "The user may not own the game or there was an error."
            )
        }

        val token = gameConnectTokens.poll() ?: throw Exception("There's no available game connect tokens left.")

        val authTicket = buildAuthTicket(token, ticketType)

        // Steam add the 'str:' prefix to the identity string itself and appends a null terminator
        val serverSecret = if (identity.isNullOrEmpty()) {
            null
        } else {
            "str:$identity\u0000".toByteArray(Charsets.UTF_8)
        }

        val (asyncJob, crc) = verifyTicket(appid, authTicket, serverSecret)
        val ticket = asyncJob.await()

        // // Verify just in case
        if (ticket.activeTicketsCRC.any { it == crc.toInt() }) {
            val tok = combineTickets(
                authTicket = authTicket,
                appTicket = appTicket.ticket,
                padToWebApiSize = ticketType == TicketType.WebApiTicket
            )
            return TicketInfo(this@SteamAuthTicket, appid, tok)
        } else {
            throw Exception("Ticket verification failed.")
        }
    }

    internal fun cancelAuthTicket(authTicket: TicketInfo) {
        synchronized(ticketChangeLock) {
            ticketsByGame[authTicket.appID]?.removeAll { it.ticketCrc == authTicket.ticketCRC.toInt() }
        }
        sendTickets()
    }

    private fun combineTickets(authTicket: ByteArray, appTicket: ByteArray, padToWebApiSize: Boolean): ByteArray {
        val len = appTicket.size

        val rawSize = authTicket.size + 4 + appTicket.size
        val target = if (padToWebApiSize) maxOf(rawSize, WEB_API_TICKET_SIZE) else rawSize

        val token = ByteArray(target)

        System.arraycopy(authTicket, 0, token, 0, authTicket.size)
        ByteBuffer.wrap(token, authTicket.size, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(len)
        System.arraycopy(appTicket, 0, token, authTicket.size + 4, appTicket.size)

        // The WebApiTicket is always 2560 bytes long, but everything after the tickets is just a trash after memory allocation
        if (padToWebApiSize && rawSize < target) {
            val random = SecureRandom()
            random.nextBytes(token.sliceArray(rawSize until target))
            System.arraycopy(token.sliceArray(rawSize until target), 0, token, rawSize, target - rawSize)
        }

        return token
    }

    /**
     * Handles generation of auth ticket.
     */
    private fun buildAuthTicket(gameConnectToken: ByteArray, ticketType: TicketType): ByteArray {
        val sessionSize =
            4 + // unknown, always 1
                4 + // TicketType, 2 or 5
                4 + // public IP v4, optional
                4 + // private IP v4, optional
                4 + // timestamp & uint.MaxValue
                4 // sequence

        MemoryStream(gameConnectToken.size + 4 + sessionSize).use { stream ->
            BinaryWriter(stream.asOutputStream()).use { writer ->
                writer.writeInt(gameConnectToken.size)
                writer.write(gameConnectToken)

                writer.writeInt(sessionSize)
                writer.writeInt(1)
                writer.writeInt(ticketType.value)

                val randomBytes = ByteArray(8)
                SecureRandom().nextBytes(randomBytes)
                writer.write(randomBytes)

                writer.writeInt(System.nanoTime().toInt())
                writer.writeInt(sequence.incrementAndGet())
            }

            return stream.toByteArray()
        }
    }

    // Note:
    // Since Kotlin doesn't have an `out` like C#, we need both crc and the job returned.
    // This is a special return case.
    private fun verifyTicket(
        appid: Int,
        authToken: ByteArray,
        serverSecret: ByteArray?,
    ): Pair<AsyncJobSingle<TicketAcceptedCallback>, Long> {
        val crc = Utils.crc32(authToken)

        synchronized(ticketChangeLock) {
            val items = ticketsByGame.computeIfAbsent(appid) { ArrayList() }
            items.add(
                SteammessagesBase.CMsgAuthTicket.newBuilder().apply {
                    this.gameid = appid.toLong()
                    this.ticket = ByteString.copyFrom(authToken)
                    this.ticketCrc = crc.toInt()
                    serverSecret?.let {
                        this.serverSecret = ByteString.copyFrom(it)
                    }
                }.build()
            )
        }

        return sendTickets() to crc
    }

    /**
     *
     */
    private fun sendTickets(): AsyncJobSingle<TicketAcceptedCallback> {
        val auth = ClientMsgProtobuf<CMsgClientAuthList.Builder>(
            CMsgClientAuthList::class.java,
            EMsg.ClientAuthList
        ).apply {
            body.tokensLeft = gameConnectTokens.size

            synchronized(ticketChangeLock) {
                body.addAllAppIds(ticketsByGame.keys)
                // Flatten map into ticket list
                body.addAllTickets(ticketsByGame.values.flatten())
            }

            sourceJobID = client.getNextJobID()
        }.also(client::send)

        return AsyncJobSingle(client, auth.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        dispatchMap[packetMsg.msgType]?.invoke(packetMsg)
    }

    // region ClientMsg Handlers

    private fun handleLogOffResponse(packetMsg: IPacketMsg) {
        // Clear all game connect tokens on client log off
        gameConnectTokens.clear()
    }

    private fun handleGameConnectTokens(packetMsg: IPacketMsg) {
        val body = ClientMsgProtobuf<CMsgClientGameConnectTokens.Builder>(
            CMsgClientGameConnectTokens::class.java,
            packetMsg
        ).body

        // Add tokens
        body.tokensList.forEach { tok ->
            gameConnectTokens.offer(tok.toByteArray())
        }

        // Keep only required amount, discard old entries
        while (gameConnectTokens.size > body.maxTokensToKeep) {
            gameConnectTokens.poll()
        }
    }

    private fun handleTicketAuthComplete(packetMsg: IPacketMsg) {
        // Ticket successfully used to authorize user
        val complete = ClientMsgProtobuf<CMsgClientTicketAuthComplete.Builder>(
            CMsgClientTicketAuthComplete::class.java,
            packetMsg
        )
        val inUse = TicketAuthCompleteCallback(complete.targetJobID, complete.body)
        client.postCallback(inUse)
    }

    private fun handleTicketAcknowledged(packetMsg: IPacketMsg) {
        // Ticket acknowledged as valid by Steam
        val authAck = ClientMsgProtobuf<CMsgClientAuthListAck.Builder>(
            CMsgClientAuthListAck::class.java,
            packetMsg
        )
        val acknowledged = TicketAcceptedCallback(authAck.targetJobID, authAck.body)
        client.postCallback(acknowledged)
    }

    // endregion
}
