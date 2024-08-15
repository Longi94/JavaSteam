package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetCDNAuthTokenResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is received when a CDN auth token is received
 */
class CDNAuthTokenCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the operation.
     */
    val result: EResult

    /**
     * Gets the CDN auth token.
     */
    val token: String

    /**
     * Gets the token expiration date.
     */
    val expiration: Date

    init {
        val response = ClientMsgProtobuf<CMsgClientGetCDNAuthTokenResponse.Builder>(
            CMsgClientGetCDNAuthTokenResponse::class.java,
            packetMsg
        )
        val msg = response.body

        jobID = response.targetJobID

        result = EResult.from(msg.eresult)
        token = msg.token
        expiration = Date(msg.expirationTime * 1000L)
    }
}
