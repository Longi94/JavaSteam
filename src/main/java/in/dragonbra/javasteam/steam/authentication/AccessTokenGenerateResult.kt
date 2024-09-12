package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails

/**
 * Represents access token generation result.
 */
class AccessTokenGenerateResult(response: CAuthentication_AccessToken_GenerateForApp_Response) {

    /**
     * New refresh token.
     * This can be provided to [LogOnDetails.accessToken]
     */
    val refreshToken: String = response.refreshToken

    /**
     * New token subordinate to [AccessTokenGenerateResult.refreshToken]
     */
    val accessToken: String = response.accessToken
}
