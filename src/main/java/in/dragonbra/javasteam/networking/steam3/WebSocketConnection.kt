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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class WebSocketConnection : Connection() {

    companion object {
        private val logger = LogManager.getLogger<WebSocketConnection>()
        private const val WATCHDOG_TIMEOUT_MS = 30_000L
        private val PING_INTERVAL = 30.toDuration(DurationUnit.SECONDS)
        private val WATCHDOG_POLL = 5.toDuration(DurationUnit.SECONDS)
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val disconnecting = AtomicBoolean(false)
    private val lastFrameTime = AtomicLong(0L)

    @Volatile private var client: HttpClient? = null

    @Volatile private var session: WebSocketSession? = null

    @Volatile private var connectionJob: Job? = null

    @Volatile private var endpoint: InetSocketAddress? = null

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        disconnecting.set(false)
        connectionJob?.cancel()

        connectionJob = scope.launch {
            logger.debug("Trying connection to ${endPoint.hostName}:${endPoint.port}")
            endpoint = endPoint
            lastFrameTime.set(System.currentTimeMillis())

            try {
                val newClient = HttpClient(CIO) {
                    install(WebSockets) {
                        pingInterval = PING_INTERVAL
                    }
                }
                client = newClient

                val newSession = newClient.webSocketSession {
                    url {
                        host = endPoint.hostName
                        port = endPoint.port
                        protocol = URLProtocol.WSS
                        path("cmsocket/")
                    }
                }
                session = newSession

                logger.debug("Connected to ${endPoint.hostName}:${endPoint.port}")
                onConnected()

                launch { runWatchdog() }

                newSession.incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Binary -> {
                            lastFrameTime.set(System.currentTimeMillis())
                            onNetMsgReceived(NetMsgEventArgs(frame.readBytes(), currentEndPoint))
                        }

                        is Frame.Close -> doDisconnect(false)

                        is Frame.Ping -> logger.debug("Received ping")

                        is Frame.Pong -> logger.debug("Received pong")

                        is Frame.Text -> logger.debug("Received text: ${frame.readText()}")
                    }
                }

                // Session ended without a Close frame
                doDisconnect(false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("An error occurred with the WebSocket connection", e)
                doDisconnect(false)
            }
        }
    }

    override fun disconnect(userInitiated: Boolean) {
        doDisconnect(userInitiated)
    }

    private fun doDisconnect(userInitiated: Boolean) {
        if (!disconnecting.compareAndSet(false, true)) return

        scope.launch {
            val currentJob = connectionJob
            val currentSession = session
            val currentClient = client
            connectionJob = null
            session = null
            client = null

            try {
                currentSession?.close()
            } catch (e: Exception) {
                logger.debug("Error closing WebSocket session: ${e.message}")
            }
            try {
                currentClient?.close()
            } catch (e: Exception) {
                logger.debug("Error closing HTTP client: ${e.message}")
            }

            currentJob?.cancel()
            currentJob?.join()

            onDisconnected(userInitiated)
        }
    }

    private suspend fun runWatchdog() {
        while (true) {
            delay(WATCHDOG_POLL)

            val elapsed = System.currentTimeMillis() - lastFrameTime.get()
            when {
                elapsed > WATCHDOG_TIMEOUT_MS -> {
                    logger.error("Watchdog: No response for ${WATCHDOG_TIMEOUT_MS / 1000} seconds, disconnecting")
                    doDisconnect(false)
                    return
                }

                elapsed > 25_000 -> logger.debug("Watchdog: No response for 25 seconds")

                elapsed > 20_000 -> logger.debug("Watchdog: No response for 20 seconds")

                elapsed > 15_000 -> logger.debug("Watchdog: No response for 15 seconds")
            }
        }
    }

    override fun send(data: ByteArray) {
        scope.launch {
            try {
                session?.send(Frame.Binary(true, data))
            } catch (e: Exception) {
                logger.error("An error occurred while sending data", e)
                doDisconnect(false)
            }
        }
    }

    override fun getLocalIP(): InetAddress = InetAddress.getLocalHost()

    override fun getCurrentEndPoint(): InetSocketAddress? = endpoint

    override fun getProtocolTypes(): ProtocolTypes = ProtocolTypes.WEB_SOCKET
}
