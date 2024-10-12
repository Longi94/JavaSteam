package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.net.URI
import java.util.concurrent.TimeUnit

internal class WebSocketCMClient(
    private val serverUri: URI?,
    timeout: Int,
    private val listener: WSListener,
) {

    companion object {
        private val logger: Logger = LogManager.getLogger(WebSocketCMClient::class.java)
    }

    // CIO doesnt support TLS 1.3 yet :/
    internal val client = HttpClient(OkHttp) {
        install(WebSockets)
        engine {
            config {
                val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_3)
                    .allEnabledCipherSuites()
                    .build()

                connectionSpecs(listOf(spec))
            }
            preconfigured = OkHttpClient.Builder()
                .pingInterval(timeout.toLong(), TimeUnit.SECONDS)
                .build()
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sendChannel = Channel<ByteArray>(capacity = 128)

    internal fun send(data: ByteArray) {
        scope.launch {
            if (!sendChannel.trySend(data).isSuccess) {
                logger.error("Send buffer is full, message dropped")
            }
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
            } catch (e: CancellationException) {
                logger.debug("Connection cancelled", e)
            } catch (e: Throwable) {
                logger.error("Unexpected error during WebSocket communication", e)
            } finally {
                logger.debug("Shutting down WebSocket connection")
                close()
            }
        }
    }

    internal fun close() {
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
}
