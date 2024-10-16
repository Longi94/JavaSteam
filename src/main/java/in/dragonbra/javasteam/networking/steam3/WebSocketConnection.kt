package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import okhttp3.Response
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

class WebSocketConnection :
    Connection(),
    WebSocketCMClient.WSListener {

    companion object {
        private val logger: Logger = LogManager.getLogger(WebSocketConnection::class.java)

        private fun constructUri(address: InetSocketAddress): URI =
            URI.create("wss://${address.hostString}:${address.port}/cmsocket/")
    }

    private val client = AtomicReference<WebSocketCMClient?>(null)

    private var socketEndPoint: InetSocketAddress? = null

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        logger.debug("Connecting to $endPoint...")

        val serverUri = constructUri(endPoint)
        val newClient = WebSocketCMClient(timeout, serverUri, this)
        val oldClient = client.getAndSet(newClient)

        oldClient?.let { oldClient ->
            logger.debug("Attempted to connect while already connected. Closing old connection...")
            oldClient.close()
            onDisconnected(false)
        }

        socketEndPoint = endPoint

        newClient.connect()
    }

    override fun disconnect(userInitiated: Boolean) {
        disconnectCore(userInitiated)
    }

    override fun send(data: ByteArray) {
        try {
            client.get()?.send(data)
        } catch (e: Exception) {
            logger.debug("Exception while sending data", e)
            disconnectCore(false)
        }
    }

    // TODO get local ip? SK uses `IPAddress.None` here.
    override fun getLocalIP(): InetAddress? = null

    override fun getCurrentEndPoint(): InetSocketAddress? = socketEndPoint

    override fun getProtocolTypes(): ProtocolTypes = ProtocolTypes.WEB_SOCKET

    private fun disconnectCore(userInitiated: Boolean) {
        logger.debug("User initiated disconnection:  $userInitiated")

        val oldClient = client.getAndSet(null)
        oldClient?.close()

        onDisconnected(userInitiated)

        socketEndPoint = null
    }

    override fun onTextData(data: String) {
        // Ignore string messages
        logger.debug("Got string message: $data")
    }

    override fun onData(data: ByteArray) {
        if (data.isNotEmpty()) {
            onNetMsgReceived(NetMsgEventArgs(data, getCurrentEndPoint()))
        }
    }

    override fun onClose(code: Int, reason: String) {
        logger.debug("Connection closed")
    }

    override fun onClosing(code: Int, reason: String) {
        logger.debug("Closing connection")
    }

    override fun onError(t: Throwable) {
        logger.error("Error in websocket", t)
        disconnectCore(false)
    }

    override fun onOpen(response: Response) {
        logger.debug("WebSocket connected to $socketEndPoint using TLS: ${response.handshake?.tlsVersion}")
        onConnected()
    }
}
