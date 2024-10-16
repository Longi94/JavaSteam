package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import java.net.URI
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketCMClient(
    timeout: Int,
    private val serverUrl: URI,
    private val listener: WSListener,
) : WebSocketListener() {

    companion object {
        private val logger = LogManager.getLogger(WebSocketCMClient::class.java)
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    /**
     * Invoked when a web socket has been accepted by the remote peer and may begin transmitting
     * messages.
     */
    override fun onOpen(webSocket: WebSocket, response: Response) {
        listener.onOpen(response)
    }

    /** Invoked when a text (type `0x1`) message has been received. */
    override fun onMessage(webSocket: WebSocket, text: String) {
        listener.onTextData(text)
    }

    /** Invoked when a binary (type `0x2`) message has been received. */
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        listener.onData(bytes.toByteArray())
    }

    /**
     * Invoked when the remote peer has indicated that no more incoming messages will be transmitted.
     */
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        listener.onClosing(code, reason)
    }

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the
     * connection has been successfully released. No further calls to this listener will be made.
     */
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        listener.onClose(true)
    }

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the
     * network. Both outgoing and incoming messages may have been lost. No further calls to this
     * listener will be made.
     */
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        listener.onError(t)
    }

    fun connect() {
        val request = Request.Builder().url(serverUrl.toString()).build()
        webSocket = client.newWebSocket(request, this)
    }

    fun send(data: ByteArray) {
        webSocket?.send(ByteString.of(*data))
    }

    fun close() {
        webSocket?.close(1000, null)

        // Shutdown the okhttp client to prevent hanging.
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
        client.cache?.close()
    }

    interface WSListener {
        fun onTextData(data: String)
        fun onData(data: ByteArray)
        fun onClose(remote: Boolean)
        fun onClosing(code: Int, reason: String)
        fun onError(t: Throwable)
        fun onOpen(response: Response)
    }
}
