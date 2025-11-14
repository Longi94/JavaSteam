package `in`.dragonbra.javasteam.steam.handlers.steamcontent

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import java.util.Date

/**
 * This is received when a CDN auth token is received
 */
class CDNAuthToken(message: ServiceMethodResponse<SteammessagesContentsystemSteamclient.CContentServerDirectory_GetCDNAuthToken_Response.Builder>) {

    /**
     * Result of the operation
     */
    val result: EResult

    /**
     * CDN auth token
     */
    val token: String

    /**
     * Token expiration date
     */
    val expiration: Date

    init {
        val response = message.body.build()
        result = message.result
        token = response.token
        expiration = Date(response.expirationTime * 1000L)
    }
}
