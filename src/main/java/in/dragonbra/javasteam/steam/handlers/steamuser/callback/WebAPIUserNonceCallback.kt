package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientRequestWebAPIAuthenticateUserNonceResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received when requesting a new WebAPI authentication user nonce.
 */
class WebAPIUserNonceCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the authentication nonce.
     */
    val nonce: String

    init {
        val userNonce = ClientMsgProtobuf<CMsgClientRequestWebAPIAuthenticateUserNonceResponse.Builder>(
            CMsgClientRequestWebAPIAuthenticateUserNonceResponse::class.java,
            packetMsg
        )
        val body = userNonce.body

        jobID = userNonce.targetJobID

        result = EResult.from(body.eresult)
        nonce = body.webapiAuthenticateUserNonce
    }
}
