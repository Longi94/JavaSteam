package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails

/**
 * Represents access token generation result.
 */
class AccessTokenGenerateResult(
    response: SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response.Builder,
) {

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
