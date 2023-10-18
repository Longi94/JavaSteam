package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient

/**
 * Represents access token generation result.
 */
class AccessTokenGenerateResult(
    response: SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response.Builder
) {

    /**
     * New refresh token.
     * This can be provided to [in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails.getAccessToken]
     */
    val refreshToken: String = response.refreshToken

    /**
     * New token subordinate to [AccessTokenGenerateResult.refreshToken]
     */
    val accessToken: String = response.accessToken
}