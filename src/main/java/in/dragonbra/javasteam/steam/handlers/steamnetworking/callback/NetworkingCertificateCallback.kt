package `in`.dragonbra.javasteam.steam.handlers.steamnetworking.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientNetworkingCertReply
import `in`.dragonbra.javasteam.steam.handlers.steamnetworking.SteamNetworking
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamNetworking.requestNetworkingCertificate].
 *
 * This can be used to populate a CMsgSteamDatagramCertificateSigned for socket communication.
 */
@Suppress("MemberVisibilityCanBePrivate")
class NetworkingCertificateCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the certificate signed by the Steam CA. This contains a CMsgSteamDatagramCertificate with the supplied public key.
     */
    val certificate: ByteArray

    /**
     * Gets the ID of the CA used to sign this certificate.
     */
    val caKeyID: Long

    /**
     * Gets the signature used to verify [NetworkingCertificateCallback.certificate]
     */
    val caSignature: ByteArray

    init {
        val resp = ClientMsgProtobuf<CMsgClientNetworkingCertReply.Builder>(
            CMsgClientNetworkingCertReply::class.java,
            packetMsg
        )
        val msg = resp.body

        jobID = resp.targetJobID

        certificate = msg.cert.toByteArray()
        caKeyID = msg.caKeyId
        caSignature = msg.caSignature.toByteArray()
    }
}
