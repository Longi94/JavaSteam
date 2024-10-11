package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.time.Duration.Companion.seconds

internal class WebSocketCMClient(
    private val serverUri: URI?,
    timeout: Int,
    private val listener: WSListener,
) {
    internal val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = timeout.seconds.inWholeMilliseconds
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sendChannel = Channel<ByteArray>(Channel.UNLIMITED)

    internal fun send(data: ByteArray) {
        scope.launch {
            sendChannel.send(data)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun connect() {
        logger.debug("Connecting to $serverUri")

        scope.launch {
            try {
                client.wss(urlString = serverUri.toString()) {
                    listener.onOpen()

                    while (isActive) {
                        if (!incoming.isEmpty) {
                            when (val frame = incoming.receive()) {
                                is Frame.Binary -> listener.onData(frame.readBytes())
                                is Frame.Close -> listener.onClose(true)
                                is Frame.Text -> logger.debug("Got string message: ${frame.readText()}")
                                else -> Unit
                            }
                        }

                        if (!sendChannel.isEmpty) {
                            val data = sendChannel.receive()
                            send(data)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    listener.onError(e)
                }
            } finally {
                internalClose()
            }
        }
    }

    internal fun close() {
        internalClose()
    }

    private fun internalClose() {
        listener.onClose(false)
        scope.cancel()
        client.close()
    }

    internal interface WSListener {
        fun onData(data: ByteArray?)
        fun onClose(remote: Boolean)
        fun onError(ex: Exception?)
        fun onOpen()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(WebSocketCMClient::class.java)
    }
}
