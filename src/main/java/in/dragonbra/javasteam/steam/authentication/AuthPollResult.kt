package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Response

/**
 * Represents authentication poll result.
 */
class AuthPollResult(response: CAuthentication_PollAuthSessionStatus_Response.Builder) {
    /**
     * Account name of authenticating account.
     */
    var accountName: String = response.accountName
        private set

    /**
     * New refresh token.
     */
    var refreshToken: String = response.refreshToken
        private set

    /**
     * Gets or Sets the new token subordinate to [refreshToken].
     */
    var accessToken: String = response.accessToken
        private set

    /**
     * May contain remembered machine ID for future login, usually when account uses email based Steam Guard.
     * Supply it in [AuthSessionDetails.guardData] for future logins to avoid resending an email.
     * This value should be stored per account.
     */
    var newGuardData: String = response.newGuardData
        private set
}