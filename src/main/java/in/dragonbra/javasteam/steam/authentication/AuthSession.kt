package `in`.dragonbra.javasteam.steam.authentication

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*
import `in`.dragonbra.javasteam.rpc.service.Authentication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Represents an authentication session which can be used to finish the authentication and get access tokens.
 */
open class AuthSession(
    /**
     * Unified messages class for Authentication related messages, see [Authentication].
     */
    var authenticationService: Authentication,
    /**
     * Confirmation types that will be able to confirm the request.
     */
    var allowedConfirmations: List<CAuthentication_AllowedConfirmation>,
    /**
     * Authenticator object which will be used to handle 2-factor authentication if necessary.
     */
    var authenticator: IAuthenticator?,
    /**
     * Unique identifier of requestor, also used for routing, portion of QR code.
     */
    var clientID: Long,
    /**
     * Unique request ID to be presented by requestor at poll time.
     */
    var requestID: ByteString,
    /**
     * Refresh interval with which requestor should call PollAuthSessionStatus.
     */
    var pollingInterval: Float
) {

    init {
        this.allowedConfirmations = sortConfirmations(allowedConfirmations)
    }

    /**
     * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
     *
     * @return An object containing tokens which can be used to log in to Steam.
     */
    fun startPolling(): AuthPollResult {
        var pollLoop = false
        var preferredConfirmation: CAuthentication_AllowedConfirmation? = allowedConfirmations.firstOrNull()

        require(!(preferredConfirmation == null || preferredConfirmation.confirmationType == EAuthSessionGuardType.k_EAuthSessionGuardType_Unknown)) {
            "There are no allowed confirmations"
        }

        // If an authenticator is provided and the device confirmation is available, allow consumers to choose whether they want to
        // simply poll until confirmation is accepted, or whether they want to fall back to the next preferred confirmation type.
        if (authenticator != null && preferredConfirmation.confirmationType == EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation) {
            val prefersToPollForConfirmation = authenticator!!.acceptDeviceConfirmation().get()

            if (!prefersToPollForConfirmation) {
                require(allowedConfirmations.size > 1) {
                    "AcceptDeviceConfirmation returned false which indicates a fallback to another confirmation type, " +
                        "but there are no other confirmation types available."
                }

                preferredConfirmation = allowedConfirmations[1]
            }
        }

        when (preferredConfirmation.confirmationType) {
            EAuthSessionGuardType.k_EAuthSessionGuardType_None -> Unit
            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode,
            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> {
                require(this is CredentialsAuthSession) {
                    "Got ${preferredConfirmation.confirmationType} confirmation type in a session that is not CredentialsAuthSession."
                }

                requireNotNull(authenticator) {
                    "This account requires an authenticator for login, but none was provided in AuthSessionDetails."
                }

                val expectedInvalidCodeResult = when (preferredConfirmation.confirmationType) {
                    EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode -> EResult.InvalidLoginAuthCode
                    EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> EResult.TwoFactorCodeMismatch
                    else -> {
                        throw IllegalArgumentException("${preferredConfirmation.confirmationType} not implemented")
                    }
                }

                var previousCodeWasIncorrect = false
                var waitingForValidCode = true

                do {
                    try {
                        val task = when (preferredConfirmation.confirmationType) {
                            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode -> {
                                authenticator!!.provideEmailCode(
                                    preferredConfirmation.associatedMessage,
                                    previousCodeWasIncorrect
                                )
                            }

                            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> {
                                authenticator!!.provideDeviceCode(previousCodeWasIncorrect)
                            }

                            else -> throw IllegalArgumentException()
                        }

                        val code = task.get()

                        require(!code.isNullOrEmpty()) { "No code was provided by the authenticator." }

                        this.sendSteamGuardCode(code, preferredConfirmation.confirmationType)

                        waitingForValidCode = false
                    } catch (e: AuthenticationException) {
                        if (e.result == expectedInvalidCodeResult) {
                            previousCodeWasIncorrect = true
                        }
                    }
                } while (waitingForValidCode)
            }

            // This is a prompt that appears in the Steam mobile app
            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation -> {
                pollLoop = true
            }

//          EAuthSessionGuardType.k_EAuthSessionGuardType_EmailConfirmation -> {
//              // TODO: what is this?
//              pollLoop = true
//          }
//          EAuthSessionGuardType.k_EAuthSessionGuardType_MachineToken -> {
//              // ${u.De.LOGIN_BASE_URL}jwt/checkdevice - with steam machine guard cookie set
//              throw IllegalArgumentException("Machine token confirmation is not supported by SteamKit at the moment.")
//          }

            else -> {
                throw IllegalArgumentException("Unsupported confirmation type ${preferredConfirmation.confirmationType}.")
            }
        }

        if (!pollLoop) {
            return pollAuthSessionStatus() ?: throw AuthenticationException(
                "Authentication failed",
                EResult.Fail
            )
        }

        var pollResponse: AuthPollResult?
        runBlocking {
            while (true) {
                delay(pollingInterval.toLong())

                pollResponse = pollAuthSessionStatus()

                if (pollResponse != null) {
                    return@runBlocking pollResponse
                }
            }
        }

        return pollResponse!!
    }

    /**
     * Polls for authentication status once. Prefer using [startPolling] instead.
     *
     * @return An object containing tokens which can be used to log in to Steam, or null if not yet authenticated.
     * @throws AuthenticationException Thrown when polling fails.
     */
    private fun pollAuthSessionStatus(): AuthPollResult? {
        val request = CAuthentication_PollAuthSessionStatus_Request.newBuilder()
        request.clientId = clientID
        request.requestId = requestID

        val message = authenticationService.PollAuthSessionStatus(request.build()).runBlock()

        // eresult can be Expired, FileNotFound, Fail
        if (message.result != EResult.OK) {
            throw AuthenticationException(
                "Failed to poll status",
                message.result
            )
        }

        val response: CAuthentication_PollAuthSessionStatus_Response.Builder =
            message.getDeserializedResponse(CAuthentication_PollAuthSessionStatus_Response::class.java)

        if (response.newClientId > 0) {
            clientID = response.newClientId
        }

        handlePollAuthSessionStatusResponse(response)

        if (response.refreshToken.isNotEmpty()) {
            return AuthPollResult(response)
        }

        return null
    }

    internal open fun handlePollAuthSessionStatusResponse(response: CAuthentication_PollAuthSessionStatus_Response.Builder) {
        if (response.newClientId != 0L) {
            clientID = response.newClientId
        }
    }

    /**
     * Sort available guard confirmation methods by an order that we prefer to handle them in
     *
     * @param confirmations the list of confirmations
     * @return a sorted list of confirmations
     */
    private fun sortConfirmations(confirmations: List<CAuthentication_AllowedConfirmation>): List<CAuthentication_AllowedConfirmation> {
        val preferredConfirmationTypes = arrayOf(
            EAuthSessionGuardType.k_EAuthSessionGuardType_None,
            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation,
            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode,
            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode,
            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailConfirmation,
            EAuthSessionGuardType.k_EAuthSessionGuardType_MachineToken,
            EAuthSessionGuardType.k_EAuthSessionGuardType_Unknown
        )

        val sortOrder = preferredConfirmationTypes.withIndex().associate { (index, value) -> value to index }

        return confirmations.sortedBy { x ->
            sortOrder[x.confirmationType] ?: Int.MAX_VALUE
        }
    }
}