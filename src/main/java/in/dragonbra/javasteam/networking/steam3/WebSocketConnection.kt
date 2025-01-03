package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds

class WebSocketConnection : Connection() {

    private val logger = LogManager.getLogger(WebSocketConnection::class.java)

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var session: DefaultWebSocketSession? = null

    private var heartbeatJob: Job? = null

    private var receiveJob: Job? = null

    private var sendJob: Job? = null

    private val isConnected = AtomicBoolean(false)

    private val currentEndPointRef = AtomicReference<InetSocketAddress>()

    private val isClosing = AtomicBoolean(false)

    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
    }

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        if (isConnected.get()) {
            logger.debug("Already connected, disconnect first")
            return
        }

        logger.debug("Connecting to ${endPoint.hostName}:${endPoint.port}")

        currentEndPointRef.set(endPoint)

        scope.launch {
            try {
                withTimeout(timeout.milliseconds) {
                    val session = client.webSocketSession {
                        url {
                            host = endPoint.hostName
                            port = endPoint.port
                            protocol = URLProtocol.WSS
                            path("cmsocket/")
                        }
                    }

                    this@WebSocketConnection.session = session
                    isConnected.set(true)
                    startHeartbeat()
                    startReceiving()

                    logger.debug("Connected to ${endPoint.hostName}:${endPoint.port}")
                    onConnected()
                }
            } catch (e: Exception) {
                logger.error("Failed to connect: ${e.message}", e)
                handleDisconnection(false)
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            logger.debug("Starting heartbeat")
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (!isConnected.get()) break

                try {
                    val currentSession = session
                    if (currentSession == null || !currentSession.isActive) {
                        logger.debug("Session became inactive during heartbeat check")
                        handleDisconnection(false)
                        break
                    }

                    withTimeout(5000) {
                        try {
                            currentSession.send(Frame.Text(""))
                        } catch (e: Exception) {
                            logger.error("Failed to send keepalive frame", e)
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error during heartbeat: ${e.message}", e)
                    handleDisconnection(false)
                    break
                }
            }
        }
    }

    private fun startReceiving() {
        receiveJob?.cancel()
        receiveJob = scope.launch {
            logger.debug("Starting receive loop")
            try {
                val incoming = session?.incoming ?: return@launch
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val data = frame.readBytes()
                            onNetMsgReceived(NetMsgEventArgs(data, currentEndPointRef.get()))
                        }

                        else -> logger.debug("Received non-binary frame: $frame")
                    }
                }
            } catch (e: Exception) {
                if (!isClosing.get()) {
                    logger.error("Error in receive loop: ${e.message}", e)
                    handleDisconnection(false)
                }
            }
        }
    }

    override fun disconnect(userInitiated: Boolean) {
        if (isClosing.getAndSet(true)) {
            logger.debug("Disconnect already in progress")
            return
        }

        scope.launch {
            try {
                logger.debug("Disconnecting... (user initiated: $userInitiated)")

                heartbeatJob?.cancelAndJoin()
                receiveJob?.cancelAndJoin()
                sendJob?.cancelAndJoin()

                val currentSession = session

                session = null
                isConnected.set(false)

                try {
                    currentSession?.close()
                } catch (e: Exception) {
                    logger.error("Error closing session", e)
                }

                val oldEndPoint = currentEndPointRef.get()

                if (oldEndPoint != null) {
                    onDisconnected(userInitiated)
                }

                currentEndPointRef.set(null)

                logger.debug("Disconnected successfully")
            } catch (e: Exception) {
                logger.error("Error during disconnect: ${e.message}", e)
            } finally {
                isClosing.set(false)
            }
        }
    }

    private fun handleDisconnection(userInitiated: Boolean) {
        disconnect(userInitiated)
    }

    override fun send(data: ByteArray) {
        if (!isConnected.get()) {
            logger.error("Cannot send: not connected")
            return
        }

        sendJob?.cancel()
        sendJob = scope.launch {
            try {
                val currentSession = session
                if (currentSession == null) {
                    logger.debug("Cannot send: session is null")
                    handleDisconnection(false)
                    return@launch
                }

                withTimeout(5000) {
                    currentSession.send(Frame.Binary(true, data))
                    logger.debug("Sent ${data.size} bytes")
                }
            } catch (e: Exception) {
                logger.error("Error sending data: ${e.message}", e)
                handleDisconnection(false)
            }
        }
    }

    override fun getLocalIP(): InetAddress = InetAddress.getLocalHost()

    override fun getCurrentEndPoint(): InetSocketAddress? = currentEndPointRef.get()

    override fun getProtocolTypes(): ProtocolTypes = ProtocolTypes.WEB_SOCKET
}
