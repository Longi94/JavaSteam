package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class WebSocketConnection :
    Connection(),
    CoroutineScope {

    companion object {
        private val logger = LogManager.getLogger(WebSocketConnection::class.java)
    }

    private val job = SupervisorJob()

    private var monitorJob: Job? = null

    private var client: HttpClient? = null

    private var session: WebSocketSession? = null

    private var endpoint: InetSocketAddress? = null

    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        launch {
            try {
                endpoint = endPoint

                client = HttpClient(CIO) {
                    install(WebSockets) {
                        pingInterval = timeout.toDuration(DurationUnit.SECONDS)
                    }
                }

                val session = client?.webSocketSession {
                    url {
                        host = endPoint.hostName
                        port = endPoint.port
                        protocol = URLProtocol.WSS
                        path("cmsocket/")
                    }
                }

                this@WebSocketConnection.session = session

                startConnectionMonitoring()

                launch {
                    try {
                        session?.incoming?.consumeEach { frame ->
                            when (frame) {
                                is Frame.Binary -> {
                                    val data = frame.readBytes()
                                    onNetMsgReceived(NetMsgEventArgs(data, currentEndPoint))
                                }

                                is Frame.Close -> disconnect(false)
                                is Frame.Ping -> logger.debug("Received pong")
                                is Frame.Pong -> logger.debug("Received pong")
                                is Frame.Text -> logger.debug("Received plain text ${frame.readText()}")
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("An error occurred while receiving data", e)
                        disconnect(false)
                    }
                }

                logger.debug("Connected to ${endPoint.hostName}:${endPoint.port}")
                onConnected()
            } catch (e: Exception) {
                logger.error("An error occurred setting up the web socket client", e)
                disconnect(false)
            }
        }
    }

    override fun disconnect(userInitiated: Boolean) {
        launch {
            try {
                monitorJob?.cancel()
                session?.close()
                client?.close()
            } finally {
                session = null
                client = null
                endpoint = null

                if (!userInitiated) {
                    job.cancelChildren()
                }
            }
        }

        onDisconnected(userInitiated)
    }

    override fun send(data: ByteArray) {
        launch {
            try {
                session?.send(Frame.Binary(true, data))
            } catch (e: Exception) {
                logger.error("An error occurred while sending data", e)
                disconnect(false)
            }
        }
    }

    override fun getLocalIP(): InetAddress = InetAddress.getLocalHost()

    override fun getCurrentEndPoint(): InetSocketAddress? = endpoint

    override fun getProtocolTypes(): ProtocolTypes = ProtocolTypes.WEB_SOCKET

    /**
     * Rudimentary watchdog
     */
    private fun startConnectionMonitoring() {
        monitorJob = launch {
            var lastFrameTime = System.currentTimeMillis()

            launch {
                session?.incoming?.consumeEach { frame ->
                    lastFrameTime = System.currentTimeMillis()
                }
            }

            while (isActive) {
                delay(5000)
                val timeSinceLastFrame = System.currentTimeMillis() - lastFrameTime

                // logger.debug("Watchdog status: $timeSinceLastFrame")
                when {
                    timeSinceLastFrame > 30000 -> {
                        logger.error("Watchdog: No response for 30 seconds. Disconnecting from steam")
                        disconnect(false)
                        break
                    }
                    timeSinceLastFrame > 20000 -> logger.debug("Watchdog: No response for 20 seconds")
                    timeSinceLastFrame > 15000 -> logger.debug("Watchdog: No response for 15 seconds")
                }
            }
        }
    }
}
