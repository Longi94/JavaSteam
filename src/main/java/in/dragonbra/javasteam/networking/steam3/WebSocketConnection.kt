package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * A [Connection] to a Steam CM over a secure WebSocket, backed by OkHttp.
 *
 * @param httpClient The base client to derive the WebSocket client from. It shares the base client's
 * connection pool, dispatcher, proxy and interceptors; only the timeouts are overridden.
 */
class WebSocketConnection @JvmOverloads constructor(
    private val httpClient: OkHttpClient = OkHttpClient(),
) : Connection() {

    companion object {
        private val logger = LogManager.getLogger<WebSocketConnection>()

        private const val NORMAL_CLOSURE = 1000
    }

    // The live connection attempt. Every teardown swaps it out, so callbacks from any other context are stale.
    private val currentContext = AtomicReference<WebSocketContext?>(null)

    @Volatile
    private var endpoint: InetSocketAddress? = null

    override val localIP: InetAddress
        get() = runCatching { InetAddress.getLocalHost() }.getOrElse { InetAddress.getByAddress(ByteArray(4)) }

    override val currentEndPoint: InetSocketAddress?
        get() = endpoint

    override val protocolTypes: ProtocolTypes
        get() = ProtocolTypes.WEB_SOCKET

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        val newContext = WebSocketContext()
        val oldContext = currentContext.getAndSet(newContext)

        if (oldContext != null) {
            logger.debug("Attempted to connect while already connected. Closing old connection...")
            oldContext.release(sendClose = true)
            onDisconnected(false)
        }

        logger.debug("Trying connection to ${endPoint.hostString}:${endPoint.port}")
        endpoint = endPoint

        val client = httpClient.newBuilder()
            // Max time to open the TCP socket, before TLS and the WebSocket upgrade
            .connectTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
            // Max time for the whole handshake; CMs in maintenance may accept TCP and never finish it
            .callTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
            // Idle is healthy before logon; CMClient's watchdog decides when silence means dead
            .readTimeout(0, TimeUnit.MILLISECONDS)
            // CMs never answer WebSocket pings, and OkHttp fails the socket on a missing pong
            .pingInterval(0, TimeUnit.MILLISECONDS)
            .build()

        // HttpUrl brackets IPv6 literals; OkHttp treats https as wss for WebSockets
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(endPoint.hostString)
            .port(endPoint.port)
            .addPathSegment("cmsocket")
            .addPathSegment("")
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        newContext.socket = client.newWebSocket(request, newContext)
    }

    override fun disconnect(userInitiated: Boolean) {
        disconnectCore(userInitiated, specificContext = null, sendClose = true)
    }

    /**
     * Retires [specificContext], or whichever context is live when it is null, and raises Disconnected for it.
     * A context can only be retired once, so racing teardowns (server close, send failure, user disconnect)
     * raise Disconnected exactly once, and a stale context can never tear down a newer one.
     *
     * Disconnected is raised before this returns, like TcpConnection.
     */
    private fun disconnectCore(userInitiated: Boolean, specificContext: WebSocketContext?, sendClose: Boolean) {
        val oldContext = if (specificContext == null) {
            currentContext.getAndSet(null)
        } else if (currentContext.compareAndSet(specificContext, null)) {
            specificContext
        } else {
            null
        }

        if (oldContext == null) {
            return
        }

        oldContext.release(sendClose)
        onDisconnected(userInitiated)
        endpoint = null
    }

    override fun send(data: ByteArray) {
        val context = currentContext.get()
        val socket = context?.socket

        if (socket == null) {
            logger.debug("Attempted to send ${data.size} bytes while not connected")
            return
        }

        // OkHttp queues sends in order; false means the socket is closing or the 16 MiB outgoing queue is full
        if (!socket.send(data.toByteString())) {
            logger.debug("Failed to queue ${data.size} bytes for sending, disconnecting")
            disconnectCore(userInitiated = false, specificContext = context, sendClose = false)
        }
    }

    /**
     * A single connection attempt and its socket. OkHttp delivers its callbacks on the socket's
     * reader thread, one at a time and in order.
     */
    private inner class WebSocketContext : WebSocketListener() {

        @Volatile
        var socket: WebSocket? = null

        // False until the WebSocket upgrade completes
        @Volatile
        private var opened = false

        private val isCurrent: Boolean
            get() = currentContext.get() === this

        fun release(sendClose: Boolean) {
            if (sendClose && opened) {
                socket?.close(NORMAL_CLOSURE, null)
            } else {
                // A failed, stalled or half-open socket may never answer a close handshake, so drop it outright
                socket?.cancel()
            }
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            // OkHttp can open on its own thread before connect() has stored the socket it returned
            socket = webSocket

            if (!isCurrent) {
                webSocket.cancel()
                return
            }

            val ep = endpoint
            logger.debug("Connected to ${ep?.hostString}:${ep?.port} using ${response.handshake?.tlsVersion}")

            opened = true

            try {
                onConnected()
            } catch (e: Exception) {
                logger.error("Exception while setting up connection to ${ep?.hostString}:${ep?.port}", e)
                disconnectCore(userInitiated = false, specificContext = this, sendClose = true)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            if (!isCurrent || bytes.size == 0) {
                return
            }

            // A throwing handler must not take the socket down with it
            try {
                onNetMsgReceived(NetMsgEventArgs(bytes.toByteArray(), endpoint))
            } catch (e: Exception) {
                logger.error("Unexpected exception propagated back to the read loop", e)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (!isCurrent) {
                return
            }

            logger.debug("Received websocket text message: \"$text\"")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            if (!isCurrent) {
                return
            }

            logger.debug("Server is closing the connection: $code $reason")

            // Answer the close frame, then report the disconnect
            disconnectCore(userInitiated = false, specificContext = this, sendClose = true)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            disconnectCore(userInitiated = false, specificContext = this, sendClose = false)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (!isCurrent) {
                return
            }

            logger.error("An error occurred with the WebSocket connection", t)
            disconnectCore(userInitiated = false, specificContext = this, sendClose = false)
        }
    }
}
