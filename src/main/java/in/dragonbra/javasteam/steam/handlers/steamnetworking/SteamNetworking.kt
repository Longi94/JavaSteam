package `in`.dragonbra.javasteam.steam.handlers.steamnetworking

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientNetworkingCertRequest
import `in`.dragonbra.javasteam.steam.handlers.steamnetworking.callback.NetworkingCertificateCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import java.util.*

/**
 * This handler is used for Steam networking sockets
 */
class SteamNetworking : ClientMsgHandler() {

    /**
     * Request a signed networking certificate from Steam for your Ed25519 public key for the given app id.
     * Results are returned in a [NetworkingCertificateCallback].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     *
     * @param appId     The App ID the certificate will be generated for.
     * @param publicKey Your Ed25519 public key.
     * @return The Job ID of the request. This can be used to find the appropriate [NetworkingCertificateCallback].
     */
    fun requestNetworkingCertificate(appId: Int, publicKey: ByteArray): AsyncJobSingle<NetworkingCertificateCallback> {
        val msg = ClientMsgProtobuf<CMsgClientNetworkingCertRequest.Builder>(
            CMsgClientNetworkingCertRequest::class.java,
            EMsg.ClientNetworkingCertRequest
        )
        msg.setSourceJobID(client.getNextJobID())

        msg.body.setAppId(appId)
        msg.body.setKeyData(ByteString.copyFrom(publicKey))

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientNetworkingCertRequestResponse -> NetworkingCertificateCallback(packetMsg)
            else -> null
        }
    }
}
