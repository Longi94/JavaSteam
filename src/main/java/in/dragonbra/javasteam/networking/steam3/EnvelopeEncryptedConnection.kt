package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.base.Msg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.generated.MsgChannelEncryptRequest
import `in`.dragonbra.javasteam.generated.MsgChannelEncryptResponse
import `in`.dragonbra.javasteam.generated.MsgChannelEncryptResult
import `in`.dragonbra.javasteam.steam.CMClient
import `in`.dragonbra.javasteam.util.IDebugNetworkListener
import `in`.dragonbra.javasteam.util.KeyDictionary
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import `in`.dragonbra.javasteam.util.crypto.RSACrypto
import `in`.dragonbra.javasteam.util.event.EventArgs
import `in`.dragonbra.javasteam.util.event.EventHandler
import `in`.dragonbra.javasteam.util.log.LogManager
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * @author lngtr
 * @since 2018-02-24
 */
class EnvelopeEncryptedConnection(
    private val inner: Connection,
    private val universe: EUniverse,
    private val debugNetworkListener: IDebugNetworkListener?,
) : Connection() {

    @Volatile
    private var state: EncryptionState = EncryptionState.Disconnected

    @Volatile
    private var encryption: INetFilterEncryption? = null

    override val currentEndPoint: InetSocketAddress?
        get() = inner.currentEndPoint

    override val protocolTypes: ProtocolTypes?
        get() = inner.protocolTypes

    override fun connect(endPoint: InetSocketAddress, timeout: Int) {
        inner.connect(endPoint, timeout)
    }

    override fun disconnect(userInitiated: Boolean) {
        inner.disconnect(userInitiated)
    }

    override val localIP: InetAddress?
        get() = inner.localIP

    init {
        inner.netMsgReceived += EventHandler(::handleNetMsgReceived)
        inner.connected += EventHandler(::handleConnected)
        inner.disconnected += EventHandler(::handleDisconnected)
    }

    override fun send(data: ByteArray) {
        if (state != EncryptionState.Encrypted) {
            inner.send(data)
            return
        }

        // A disconnect on the network thread can clear the encryption between the state check and here
        val currentEncryption = encryption

        if (currentEncryption == null) {
            logger.debug("Attempting to send client data when not connected.")
            return
        }

        inner.send(currentEncryption.processOutgoing(data))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleConnected(sender: Any, e: EventArgs) {
        state = EncryptionState.Connected
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleDisconnected(sender: Any, e: DisconnectedEventArgs) {
        state = EncryptionState.Disconnected
        encryption = null
        disconnected.invoke(this, e)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleNetMsgReceived(sender: Any, e: NetMsgEventArgs) {
        if (state == EncryptionState.Disconnected) {
            return
        }

        if (state == EncryptionState.Encrypted) {
            // A disconnect on another thread can clear the encryption between the state check and here
            val currentEncryption = encryption ?: return

            val plaintextData: ByteArray

            try {
                plaintextData = currentEncryption.processIncoming(e.data)
            } catch (_: IOException) {
                logger.debug("Failed to decrypt incoming packet, shutting down connection")
                disconnect(userInitiated = false)
                return
            }

            netMsgReceived.invoke(this, e.withData(plaintextData))
            return
        }

        val packetMsg = CMClient.getPacketMsg(e.data)

        if (packetMsg == null) {
            logger.debug("Failed to parse message during channel setup, shutting down connection")
            disconnect(userInitiated = false)
            return
        }

        debugNetworkListener?.onIncomingNetworkMessage(packetMsg.msgType, packetMsg.data)

        if (!isExpectedEMsg(packetMsg.msgType)) {
            logger.debug("Rejected EMsg: ${packetMsg.msgType} during channel setup")
            return
        }

        when (packetMsg.msgType) {
            EMsg.ChannelEncryptRequest -> handleEncryptRequest(packetMsg)
            EMsg.ChannelEncryptResult -> handleEncryptResult(packetMsg)
            else -> Unit
        }
    }

    private fun handleEncryptRequest(packetMsg: IPacketMsg) {
        val request = Msg(MsgChannelEncryptRequest::class.java, packetMsg)

        val connectedUniverse = request.body.universe
        val protoVersion = request.body.protocolVersion

        logger.debug("Got encryption request. Universe: $connectedUniverse Protocol ver: $protoVersion")

        if (protoVersion != MsgChannelEncryptRequest.PROTOCOL_VERSION) {
            logger.debug("Encryption handshake protocol version mismatch!")
        }

        if (connectedUniverse != universe) {
            logger.debug("Expected universe $universe but server reported universe $connectedUniverse")
        }

        var randomChallenge: ByteArray? = null
        if (request.payload.length >= 16) {
            randomChallenge = request.payload.toByteArray()
        }

        val publicKey = KeyDictionary.getPublicKey(connectedUniverse)

        if (publicKey == null) {
            logger.debug("HandleEncryptRequest got request for invalid universe! Universe: $connectedUniverse Protocol ver: $protoVersion")
            disconnect(userInitiated = false)
            return
        }

        val response = Msg(MsgChannelEncryptResponse::class.java)
        val tempSessionKey = CryptoHelper.generateRandomBlock(32)

        val rsa = RSACrypto(publicKey)

        val encryptedHandshakeBlob = if (randomChallenge != null) {
            val blobToEncrypt = ByteArray(tempSessionKey.size + randomChallenge.size)

            System.arraycopy(tempSessionKey, 0, blobToEncrypt, 0, tempSessionKey.size)
            System.arraycopy(randomChallenge, 0, blobToEncrypt, tempSessionKey.size, randomChallenge.size)

            rsa.encrypt(blobToEncrypt)
        } else {
            rsa.encrypt(tempSessionKey)
        } ?: run {
            logger.debug("Failed to encrypt the handshake blob, shutting down connection")
            disconnect(userInitiated = false)
            return
        }

        val keyCrc = CryptoHelper.crcHash(encryptedHandshakeBlob)

        try {
            response.writeBytes(encryptedHandshakeBlob)
            response.writeBytes(keyCrc)
            response.writeInt(0)
        } catch (e: IOException) {
            logger.debug(e)
        }

        encryption = if (randomChallenge != null) {
            NetFilterEncryptionWithHMAC(tempSessionKey)
        } else {
            NetFilterEncryption(tempSessionKey)
        }

        val serialized = response.serialize()

        debugNetworkListener?.onOutgoingNetworkMessage(response.msgType, serialized)

        state = EncryptionState.Challenged
        send(serialized)
    }

    private fun handleEncryptResult(packetMsg: IPacketMsg) {
        val result = Msg(MsgChannelEncryptResult::class.java, packetMsg)

        logger.debug("Encryption result: ${result.body.result}")
        // DebugLog.Assert( encryption != null, nameof( EnvelopeEncryptedConnection ), "Encryption is null" )

        if (result.body.result == EResult.OK && encryption != null) {
            state = EncryptionState.Encrypted
            connected.invoke(this, EventArgs.EMPTY)
        } else {
            logger.debug("Encryption channel setup failed")
            disconnect(userInitiated = false)
        }
    }

    private fun isExpectedEMsg(msg: EMsg): Boolean = when (state) {
        EncryptionState.Disconnected -> false
        EncryptionState.Connected -> msg == EMsg.ChannelEncryptRequest
        EncryptionState.Challenged -> msg == EMsg.ChannelEncryptResult
        EncryptionState.Encrypted -> true
    }

    enum class EncryptionState {
        Disconnected,
        Connected,
        Challenged,
        Encrypted,
    }

    companion object {
        private val logger = LogManager.getLogger(EnvelopeEncryptedConnection::class.java)
    }
}
