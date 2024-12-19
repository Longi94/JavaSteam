package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext

class WebSocketConnection :
    Connection(),
    CoroutineScope {

    companion object {
        private val logger: Logger = LogManager.getLogger(WebSocketConnection::class.java)
    }

    private var client: DefaultClientWebSocketSession? = null

    private var currentTimeout: Long = 5000L

    private var currentEndpoint: InetSocketAddress? = null

    private val job = SupervisorJob()

    private val ktorClient = HttpClient(CIO) {
        install(WebSockets)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + CoroutineName("WebSocketConnection")

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        currentEndpoint = endPoint
        currentTimeout = timeout.toLong()
        launch {
            try {
                val session = withTimeout(currentTimeout) {
                    ktorClient.webSocketSession {
                        url {
                            protocol = URLProtocol.WSS
                            host = endPoint.hostString
                            port = endPoint.port
                            path("cmsocket/")
                        }
                    }
                }

                client = session
                onConnected()

                logger.debug("Connected to ${endPoint.hostString}:${endPoint.port}")

                for (frame in session.incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val event = NetMsgEventArgs(frame.data, currentEndpoint)
                            onNetMsgReceived(event)
                        }

                        is Frame.Close -> {
                            disconnect(false)
                            break
                        }

                        else -> Unit // Ignore other frames
                    }
                }
            } catch (e: Exception) {
                logger.debug("An error occurred in the WebSocket connection", e)
                onDisconnected(false)
            }
        }
    }

    override fun disconnect(userInitiated: Boolean) {
        logger.debug("Disconnecting from $currentEndpoint, userInitiated: $userInitiated")
        launch {
            try {
                client?.close()
            } finally {
                client = null
                currentEndpoint = null
                onDisconnected(userInitiated)
            }
        }
    }

    override fun send(data: ByteArray?) {
        launch {
            if (client == null) {
                logger.debug("Attempted to send data while not connected")
                return@launch
            }

            if (data != null && data.isNotEmpty()) {
                withTimeout(currentTimeout) {
                    val frame = Frame.Binary(true, data)
                    client?.send(frame)
                }
            }
        }
    }

    override fun getLocalIP(): InetAddress? = InetAddress.getLocalHost()

    override fun getCurrentEndPoint(): InetSocketAddress? = currentEndpoint

    override fun getProtocolTypes(): ProtocolTypes? = ProtocolTypes.WEB_SOCKET
}
