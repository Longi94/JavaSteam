package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.EAuthSessionGuardType
import `in`.dragonbra.javasteam.rpc.service.Authentication
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Credentials based authentication session.
 */
class CredentialsAuthSession(
    service: Authentication,
    authenticator: IAuthenticator?,
    response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder
) : AuthSession(
    authenticationService = service,
    authenticator = authenticator,
    clientID = response.clientId,
    requestID = response.requestId,
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

        val message = authenticationService.UpdateAuthSessionWithSteamGuardCode(request.build()).runBlock()

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