package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * QR code based authentication session.
 */
class QrAuthSession @JvmOverloads constructor(
    authentication: SteamAuthentication,
    authenticator: IAuthenticator?,
    response: CAuthentication_BeginAuthSessionViaQR_Response.Builder,
    defaultScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : AuthSession(
    authentication = authentication,
    authenticator = authenticator,
    clientID = response.clientId,
    requestID = response.requestId.toByteArray(),
    allowedConfirmations = response.allowedConfirmationsList,
    pollingInterval = response.interval,
    defaultScope = defaultScope,
) {

    /**
     * URL based on client ID, which can be rendered as QR code.
     */
    var challengeUrl: String = response.challengeUrl
        private set

    /**
     * Called whenever the challenge url is refreshed by Steam.
     */
    var challengeUrlChanged: IChallengeUrlChanged? = null

    override fun handlePollAuthSessionStatusResponse(response: CAuthentication_PollAuthSessionStatus_Response.Builder) {
        super.handlePollAuthSessionStatusResponse(response)

        if (response.newChallengeUrl.isNotEmpty()) {
            challengeUrl = response.newChallengeUrl
            challengeUrlChanged?.onChanged(this)
        }
    }
}
