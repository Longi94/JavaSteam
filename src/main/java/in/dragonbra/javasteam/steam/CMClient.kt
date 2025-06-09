package `in`.dragonbra.javasteam.steam

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.base.PacketClientMsg
import `in`.dragonbra.javasteam.base.PacketClientMsgProtobuf
import `in`.dragonbra.javasteam.base.PacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.generated.MsgClientLogon
import `in`.dragonbra.javasteam.generated.MsgClientServerUnavailable
import `in`.dragonbra.javasteam.networking.steam3.Connection
import `in`.dragonbra.javasteam.networking.steam3.DisconnectedEventArgs
import `in`.dragonbra.javasteam.networking.steam3.IConnectionFactory
import `in`.dragonbra.javasteam.networking.steam3.NetMsgEventArgs
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgMulti
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientSessionToken
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientHeartBeat
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientHello
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLoggedOff
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse
import `in`.dragonbra.javasteam.steam.discovery.ServerQuality
import `in`.dragonbra.javasteam.steam.discovery.ServerRecord
import `in`.dragonbra.javasteam.steam.discovery.SmartCMServerList
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.IDebugNetworkListener
import `in`.dragonbra.javasteam.util.MsgUtil
import `in`.dragonbra.javasteam.util.NetHelpers
import `in`.dragonbra.javasteam.util.NetHookNetworkListener
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.event.EventArgs
import `in`.dragonbra.javasteam.util.event.EventHandler
import `in`.dragonbra.javasteam.util.event.ScheduledFunction
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import okio.Buffer
import okio.Source
import okio.buffer
import okio.gzip
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.EnumSet

/**
 * This base client handles the underlying connection to a CM server. This class should not be use directly,
 * but through the [SteamClient] class.
 *
 * @constructor Initializes a new instance of the [CMClient] class with a specific configuration.
 * @param configuration The configuration to use for this client.
 * @param identifier A specific identifier to be used to uniquely identify this instance.
 * @throws IllegalArgumentException The identifier is an empty string
 */
@Suppress("unused")
abstract class CMClient
@Throws(IllegalArgumentException::class)
constructor(
    val configuration: SteamConfiguration,
    val identifier: String,
) {
    companion object {
        private val logger: Logger = LogManager.getLogger(CMClient::class.java)

        // TODO move to Utils once that is ported to Kotlin
        fun Long.toSteamID(): SteamID = SteamID(this)

        @JvmStatic
        fun getPacketMsg(data: ByteArray): IPacketMsg? {
            if (data.size < 4) {
                logger.debug(
                    "PacketMsg too small to contain a message, was only ${data.size} bytes. " +
                        "Message: ${Strings.toHex(data)}"
                )
                return null
            }

            var rawEMsg = 0
            BinaryReader(ByteArrayInputStream(data)).use { reader ->
                rawEMsg = reader.readInt()
            }
            val eMsg: EMsg = MsgUtil.getMsg(rawEMsg)

            when (eMsg) {
                EMsg.ChannelEncryptRequest,
                EMsg.ChannelEncryptResponse,
                EMsg.ChannelEncryptResult,
                -> try {
                    return PacketMsg(eMsg, data)
                } catch (e: IOException) {
                    logger.debug("Exception deserializing emsg $eMsg (${MsgUtil.isProtoBuf(rawEMsg)}).", e)
                }

                else -> Unit
            }

            try {
                return if (MsgUtil.isProtoBuf(rawEMsg)) {
                    // if the emsg is flagged, we're a proto message
                    PacketClientMsgProtobuf(eMsg, data)
                } else {
                    PacketClientMsg(eMsg, data)
                }
            } catch (e: IOException) {
                logger.debug("Exception deserializing emsg $eMsg (${MsgUtil.isProtoBuf(rawEMsg)}).", e)
                return null
            }
        }
    }

    /**
     * Bootstrap list of CM servers.
     */
    val servers: SmartCMServerList
        get() = configuration.serverList

    /**
     * Returns the local IP of this client.
     */
    val localIP: InetAddress?
        get() = connection?.localIP

    /**
     * Returns the current endpoint this client is connected to.
     */
    val currentEndPoint: InetSocketAddress?
        get() = connection?.currentEndPoint

    /**
     * Gets the public IP address of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    var publicIP: InetAddress? = null
        private set

    /**
     * Gets the country code of our public IP address according to Steam. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    var ipCountryCode: String? = null
        private set

    /**
     * Gets the universe of this client.
     */
    val universe: EUniverse
        get() = configuration.universe

    /**
     * Gets a value indicating whether this instance is connected to the remote CM server.
     * <c>true</c> if this instance is connected; otherwise, <c>false</c>.
     */
    var isConnected: Boolean = false
        private set

    /**
     * Gets a value indicating whether isConnected and connection is not connected to the remote CM server.
     * Inverse alternative to [CMClient.isConnected]
     * @return **true** is this instance is disconnected, otherwise, **false**.
     */
    // JavaSteam addition: "since the client can technically not be connected but still have a connection"
    fun isDisconnected(): Boolean = !isConnected && connection == null

    /**
     * Gets the session token assigned to this client from the AM.
     */
    var sessionToken: Long = 0L
        private set

    /**
     * Gets the Steam recommended Cell ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    var cellID: Int? = null
        private set

    /**
     * Gets the session ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    var sessionID: Int? = null
        private set

    /**
     * Gets the SteamID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    var steamID: SteamID? = null
        private set

    /**
     * Gets or sets the connection timeout used when connecting to the Steam server.
     */
    val connectionTimeout: Long
        get() = configuration.connectionTimeout

    /**
     * Gets or sets the network listening interface. Use this for debugging only.
     * For your convenience, you can use [NetHookNetworkListener] class.
     */
    var debugNetworkListener: IDebugNetworkListener? = null

    /**
     *
     */
    var expectDisconnection: Boolean = false

    // connection lock around the setup and tear down of the connection task
    private val connectionLock: Any = Any()

    @Volatile
    private var connection: Connection? = null

    val heartBeatFunc: ScheduledFunction

    init {
        if (identifier.isBlank()) {
            throw IllegalArgumentException("identifier must not be empty")
        }

        heartBeatFunc = ScheduledFunction({
            val heartbeat = ClientMsgProtobuf<CMsgClientHeartBeat.Builder>(
                CMsgClientHeartBeat::class.java,
                EMsg.ClientHeartBeat
            ).apply {
                body.sendReply = true // Ping Pong
            }
            send(heartbeat)
        }, 5000)
    }

    /**
     * Connects this client to a Steam3 server. This begins the process of connecting and encrypting the data channel
     * between the client and the server. Results are returned asynchronously in a [ConnectedCallback][in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback]. If the
     * server that SteamKit attempts to connect to is down, a [DisconnectedCallback][in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback] will be posted instead.
     * SteamKit will not attempt to reconnect to Steam, you must handle this callback and call Connect again preferably
     * after a short delay.
     *
     * @param cmServer The [ServerRecord] of the CM server to connect to.
     */
    @JvmOverloads
    fun connect(cmServer: ServerRecord? = null) {
        var cmServer = cmServer
        synchronized(connectionLock) {
            try {
                disconnect(true)

                assert(connection == null)

                expectDisconnection = false

                if (cmServer == null) {
                    cmServer = servers.getNextServerCandidate(configuration.protocolTypes)
                }

                if (cmServer == null) {
                    logger.error("No CM servers available to connect to")
                    onClientDisconnected(false)
                    return
                }

                connection = createConnection(cmServer.protocolTypes)
                connection!!.getNetMsgReceived().addEventHandler(netMsgReceived)
                connection!!.getConnected().addEventHandler(connected)
                connection!!.getDisconnected().addEventHandler(disconnected)
                logger.debug(
                    String.format(
                        "Connecting to %s with protocol %s, and with connection impl %s",
                        cmServer.endpoint,
                        cmServer.protocolTypes,
                        connection!!.javaClass.getSimpleName()
                    )
                )
                connection!!.connect(cmServer.endpoint)
            } catch (e: Exception) {
                logger.debug("Failed to connect to Steam network", e)
                onClientDisconnected(false)
            }
        }
    }

    /**
     * Disconnects this client.
     */
    @JvmOverloads
    fun disconnect(userInitiated: Boolean = true) {
        synchronized(connectionLock) {
            heartBeatFunc.stop()
            if (connection != null) {
                connection!!.disconnect(userInitiated)
            }
        }
    }

    /**
     * Sends the specified client message to the server.
     * This method automatically assigns the correct [sessionID] and [SteamID] of the message.
     * @param msg The client message to send.
     */
    fun send(msg: IClientMsg) {
        if (!isConnected) {
            logger.error("Send(${msg.msgType}) was called while not connected to Steam.")
        }

        sessionID?.let { msg.setSessionID(it) }
        steamID?.let { msg.setSteamID(it) }

        try {
            debugNetworkListener?.onOutgoingNetworkMessage(msg.getMsgType(), msg.serialize())
        } catch (e: Exception) {
            logger.debug("DebugNetworkListener threw an exception", e)
        }

        // we'll swallow any network failures here because they will be thrown later
        // on the network thread, and that will lead to a disconnect callback
        // down the line
        connection?.send(msg.serialize())
    }

    // TODO: override fun logDebug()

    /**
     * Called when a client message is received from the network.
     * @param packetMsg The packet message.
     */
    protected open fun onClientMsgReceived(packetMsg: IPacketMsg?): Boolean {
        if (packetMsg == null) {
            logger.debug("Packet message failed to parse, shutting down connection")
            disconnect(userInitiated = false)
            return false
        }

        // Multi message gets logged down the line after it's decompressed
        if (packetMsg.getMsgType() != EMsg.Multi) {
            try {
                debugNetworkListener?.onIncomingNetworkMessage(packetMsg.getMsgType(), packetMsg.getData())
            } catch (e: Exception) {
                logger.debug("debugNetworkListener threw an exception", e)
            }
        }

        when (packetMsg.getMsgType()) {
            EMsg.Multi -> handleMulti(packetMsg)
            EMsg.ClientLogOnResponse -> handleLogOnResponse(packetMsg) // we handle this to get the SteamID/SessionID and to setup heartbeating
            EMsg.ClientLoggedOff -> handleLoggedOff(packetMsg) // to stop heartbeating when we get logged off
            EMsg.ClientServerUnavailable -> handleServerUnavailable(packetMsg)
            EMsg.ClientSessionToken -> handleSessionToken(packetMsg) // am session token
            else -> Unit
        }

        return true
    }

    /**
     * Called when the client is securely isConnected to Steam3.
     */
    protected open fun onClientConnected() {
        val request = ClientMsgProtobuf<CMsgClientHello.Builder>(
            CMsgClientHello::class.java,
            EMsg.ClientHello
        ).apply {
            body.protocolVersion = MsgClientLogon.CurrentProtocol
        }

        send(request)
    }

    /**
     * Called when the client is physically disconnected from Steam3.
     * @param userInitiated whether the disconnect was initialized by the client
     */
    protected open fun onClientDisconnected(userInitiated: Boolean) {}

    private fun createConnection(protocol: EnumSet<ProtocolTypes>): Connection {
        val connectionFactory: IConnectionFactory = configuration.connectionFactory
        val connection = connectionFactory.createConnection(configuration, protocol)
        if (connection == null) {
            logger.error(String.format("Connection factory returned null connection for protocols %s", protocol))
            throw IllegalArgumentException("Connection factory returned null connection.")
        }
        return connection
    }

    /**
     * Debugging: Do not use this directly.
     */
    fun receiveTestPacketMsg(packetMsg: IPacketMsg) {
        onClientMsgReceived(packetMsg)
    }

    /**
     * Debugging: Do not use this directly.
     */
    fun setIsConnected(value: Boolean) {
        isConnected = value
    }

    private val netMsgReceived = EventHandler<NetMsgEventArgs> { _, e ->
        onClientMsgReceived(getPacketMsg(e.data))
    }

    private val connected = EventHandler<EventArgs> { _, e ->
        logger.debug("EventHandler `connected` called")

        if (connection == null) {
            logger.error("No connection object after connecting.")
        }

        if (connection?.currentEndPoint == null) {
            logger.error("No connection endpoint after connecting - cannot update server list")
        }

        servers.tryMark(
            endPoint = connection!!.getCurrentEndPoint(),
            protocolTypes = connection!!.getProtocolTypes(),
            quality = ServerQuality.GOOD
        )

        isConnected = true

        try {
            onClientConnected()
        } catch (ex: Exception) {
            logger.error("Unhandled exception after connecting: ", ex)
            disconnect(userInitiated = false)
        }
    }

    private val disconnected = object : EventHandler<DisconnectedEventArgs> {
        override fun handleEvent(
            sender: Any?,
            e: DisconnectedEventArgs,
        ) {
            logger.debug(
                "EventHandler `disconnected` called. User Initiated: ${e.isUserInitiated}, " +
                    "Expected Disconnection: $expectDisconnection"
            )
            isConnected = false

            if (!e.isUserInitiated && !expectDisconnection) {
                servers.tryMark(
                    endPoint = connection!!.currentEndPoint,
                    protocolTypes = connection!!.protocolTypes,
                    quality = ServerQuality.BAD
                )
            }

            sessionID = null
            steamID = null

            connection!!.getNetMsgReceived().removeEventHandler(netMsgReceived)
            connection!!.getConnected().removeEventHandler(connected)
            connection!!.getDisconnected().removeEventHandler(this)
            connection = null // Why do we null here, but SK doesn't?

            heartBeatFunc.stop()

            onClientDisconnected(userInitiated = e.isUserInitiated || expectDisconnection)
        }
    }

    // region ClientMsg Handlers

    private fun handleMulti(packetMsg: IPacketMsg) {
        if (!packetMsg.isProto()) {
            logger.debug("HandleMulti got non-proto MsgMulti!!")
            return
        }

        val msgMulti = ClientMsgProtobuf<CMsgMulti.Builder>(CMsgMulti::class.java, packetMsg)

        val payloadBuffer = Buffer().write(msgMulti.body.messageBody.toByteArray()).let {
            if (msgMulti.body.sizeUnzipped > 0) {
                (it as Source).gzip().buffer()
            } else {
                it
            }
        }

        do {
            val packetSize = payloadBuffer.readIntLe()
            val packetContent = payloadBuffer.readByteArray(packetSize.toLong())
            val packet = getPacketMsg(packetContent)

            if (!onClientMsgReceived(packet)) {
                break
            }
        } while (!payloadBuffer.exhausted())

        payloadBuffer.close()
    }

    private fun handleLogOnResponse(packetMsg: IPacketMsg) {
        if (!packetMsg.isProto()) {
            // a non-proto ClientLogonResponse can come in as a result of connecting but never sending a ClientLogon
            // in this case, it always fails, so we don't need to do anything special here
            logger.debug("Got non-proto logon response, this is indicative of no logon attempt after connecting.")
            return
        }

        val logonResp = ClientMsgProtobuf<CMsgClientLogonResponse.Builder>(
            CMsgClientLogonResponse::class.java,
            packetMsg
        )
        val logonResponse: EResult = EResult.from(logonResp.body.eresult)
        val extendedResponse: EResult = EResult.from(logonResp.body.eresultExtended)

        logger.debug("handleLogOnResponse got response: $logonResponse, extended: $extendedResponse")

        // Note: Sometimes if you sign in too many times, steam may confuse "InvalidPassword" with "RateLimitExceeded"
        if (logonResponse == EResult.OK) {
            sessionID = logonResp.protoHeader.clientSessionid
            steamID = logonResp.protoHeader.steamid.toSteamID()

            cellID = logonResp.body.cellId
            publicIP = NetHelpers.getIPAddress(logonResp.body.publicIp)
            ipCountryCode = logonResp.body.ipCountryCode

            val hbDelay = logonResp.body.legacyOutOfGameHeartbeatSeconds

            // restart heartbeat
            heartBeatFunc.stop()
            heartBeatFunc.delay = hbDelay * 1000L
            heartBeatFunc.start()
        } else if (logonResponse == EResult.TryAnotherCM || logonResponse == EResult.ServiceUnavailable) {
            if (connection?.currentEndPoint != null) {
                servers.tryMark(
                    endPoint = connection?.currentEndPoint,
                    protocolTypes = connection?.protocolTypes,
                    quality = ServerQuality.BAD
                )
            }
        }
    }

    private fun handleLoggedOff(packetMsg: IPacketMsg) {
        sessionID = null
        steamID = null

        cellID = null
        publicIP = null
        ipCountryCode = null

        heartBeatFunc.stop()

        if (packetMsg.isProto()) {
            val logoffMsg = ClientMsgProtobuf<CMsgClientLoggedOff.Builder>(
                CMsgClientLoggedOff::class.java,
                packetMsg
            )
            val logoffResult = EResult.from(logoffMsg.body.eresult)

            logger.debug("handleLoggedOff got $logoffResult")

            if (logoffResult == EResult.TryAnotherCM || logoffResult == EResult.ServiceUnavailable) {
                if (connection == null) {
                    logger.error("No connection object during ClientLoggedOff.")
                }

                if (connection?.currentEndPoint == null) {
                    logger.error("No connection endpoint during ClientLoggedOff - cannot update server list status")
                }

                servers.tryMark(
                    endPoint = connection?.currentEndPoint,
                    protocolTypes = connection?.protocolTypes,
                    quality = ServerQuality.BAD
                )
            }
        } else {
            logger.debug("handleLoggedOff got unexpected response: ${packetMsg.getMsgType()}")
        }
    }

    private fun handleServerUnavailable(packetMsg: IPacketMsg) {
        val msgServerUnavailable = ClientMsg(MsgClientServerUnavailable::class.java, packetMsg)

        logger.debug(
            "A server of type ${msgServerUnavailable.body.eServerTypeUnavailable} " +
                "was not available for request: ${EMsg.from(msgServerUnavailable.body.eMsgSent)}"
        )

        disconnect(userInitiated = false)
    }

    private fun handleSessionToken(packetMsg: IPacketMsg) {
        val sessToken = ClientMsgProtobuf<CMsgClientSessionToken.Builder>(
            CMsgClientSessionToken::class.java,
            packetMsg
        )

        sessionToken = sessToken.body.token
    }

    // endregion
}
