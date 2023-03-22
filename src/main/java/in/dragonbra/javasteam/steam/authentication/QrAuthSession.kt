package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Response
import `in`.dragonbra.javasteam.rpc.service.Authentication

/**
 * Interface to tell the listening class that the QR Challenge URL has changed.
 */
interface OnChallengeUrlChanged {
    fun onChanged(qrAuthSession: QrAuthSession)
}

/**
 * QR code based authentication session.
 */
class QrAuthSession(
    service: Authentication,
    authenticator: IAuthenticator?,
    response: CAuthentication_BeginAuthSessionViaQR_Response.Builder
) : AuthSession(
    authenticationService = service,
    authenticator = authenticator,
    clientID = response.clientId,
    requestID = response.requestId,
    allowedConfirmations = response.allowedConfirmationsList,
    pollingInterval = response.interval
) {
    /**
     * URL based on client ID, which can be rendered as QR code.
     */
    var challengeUrl: String = response.challengeUrl

    /**
     * Called whenever the challenge url is refreshed by Steam.
     */
    var challengeUrlChanged: OnChallengeUrlChanged? = null

    override fun handlePollAuthSessionStatusResponse(response: CAuthentication_PollAuthSessionStatus_Response.Builder) {
        super.handlePollAuthSessionStatusResponse(response)

        if (response.newChallengeUrl.isNotEmpty()) {
            challengeUrl = response.newChallengeUrl
            challengeUrlChanged?.onChanged(this)
        }
    }
}