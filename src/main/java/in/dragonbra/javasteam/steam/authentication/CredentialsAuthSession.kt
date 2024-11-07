package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.EAuthSessionGuardType
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Credentials based authentication session.
 */
class CredentialsAuthSession(
    authentication: SteamAuthentication,
    authenticator: IAuthenticator?,
    response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder,
) : AuthSession(
    authentication = authentication,
    authenticator = authenticator,
    clientID = response.clientId,
    requestID = response.requestId.toByteArray(),
    allowedConfirmations = response.allowedConfirmationsList,
    pollingInterval = response.interval
) {

    // SteamID of the account logging in, will only be included if the credentials were correct.
    private val steamID: SteamID = SteamID(response.steamid)

    /**
     * Send Steam Guard code for this authentication session.
     * @param code     The code.
     * @param codeType Type of code.
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class)
    fun sendSteamGuardCode(code: String?, codeType: EAuthSessionGuardType?) {
        val request = CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request.newBuilder().apply {
            this.clientId = clientID
            this.steamid = steamID.convertToUInt64()
            this.code = code
            this.codeType = codeType
        }

        val response = authentication.authenticationService
            .updateAuthSessionWithSteamGuardCode(request.build())
            .runBlock()

        // Observed results can be InvalidLoginAuthCode, TwoFactorCodeMismatch, Expired, DuplicateRequest.
        // DuplicateRequest happens when accepting the prompt in the mobile app, and then trying to send guard code here,
        // we do not throw on it here because authentication will succeed on the next poll.
        if (response.result != EResult.OK && response.result != EResult.DuplicateRequest) {
            throw AuthenticationException("Failed to send steam guard code", response.result)
        }

        // response may contain agreement_session_url
    }
}
