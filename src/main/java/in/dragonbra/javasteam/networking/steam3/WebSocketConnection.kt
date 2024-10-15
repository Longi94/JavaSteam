package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
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

    private var userInitiated = false

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        logger.debug("Connecting to $endPoint...")

        val serverUri = constructUri(endPoint)
        val newClient = WebSocketCMClient(timeout, serverUri, this)
        val oldClient = client.getAndSet(newClient)

        oldClient?.let { oldClient ->
            logger.debug("Attempted to connect while already connected. Closing old connection...")
            oldClient.close()
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

    override fun getLocalIP(): InetAddress? {
        // TODO get local ip
        logger.debug("TODO getLocalIP()")
        return null
    }

    override fun getCurrentEndPoint(): InetSocketAddress? = socketEndPoint

    override fun getProtocolTypes(): ProtocolTypes = ProtocolTypes.WEB_SOCKET

    private fun disconnectCore(userInitiated: Boolean) {
        val oldClient = client.getAndSet(null)

        oldClient?.close()
        this.userInitiated = userInitiated
        this.socketEndPoint = null
    }

    override fun onData(data: ByteArray) {
        if (data.isNotEmpty()) {
            onNetMsgReceived(NetMsgEventArgs(data, getCurrentEndPoint()))
        }
    }

    override fun onClose(remote: Boolean) {
        onDisconnected(userInitiated && !remote)
    }

    override fun onError(t: Throwable) {
        logger.error("Error in websocket", t)
    }

    override fun onOpen() {
        onConnected()
    }
}
