package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Credentials based authentication session.
 */
class CredentialsAuthSession(
    authentication: SteamAuthentication,
    authenticator: IAuthenticator?,
    response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder
) : AuthSession(
    authentication = authentication,
    authenticator = authenticator,
    clientId = response.clientId,
    requestId = response.requestId.toByteArray(),
    allowedConfirmations = response.allowedConfirmationsList,
    pollingInterval = response.interval
) {

    // SteamID of the account logging in, will only be included if the credentials were correct.
    private val steamID: SteamID = SteamID(response.steamid)

    /**
     * Send Steam Guard code for this authentication session.
     *
     * @param code     The code.
     * @param codeType Type of code.
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class)
    fun sendSteamGuardCode(code: String?, codeType: EAuthSessionGuardType?) {
        val request = CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request.newBuilder()
        request.clientId = clientID
        request.steamid = steamID.convertToUInt64()
        request.code = code
        request.codeType = codeType

        val message = authentication.authenticationService.UpdateAuthSessionWithSteamGuardCode(request.build()).runBlock()

        val response: CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response.Builder =
            message.getDeserializedResponse(CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response::class.java)

        // can be InvalidLoginAuthCode, TwoFactorCodeMismatch, Expired
        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to send steam guard code", message.result)
        }

        // response may contain agreement_session_url
        response.agreementSessionUrl // Useless, just stops `Variable 'response' is never used`
    }
}