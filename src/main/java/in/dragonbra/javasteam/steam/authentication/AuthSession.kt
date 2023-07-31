package `in`.dragonbra.javasteam.steam.authentication

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*
import `in`.dragonbra.javasteam.rpc.service.Authentication
import kotlinx.coroutines.*

/**
 * Represents an authentication session which can be used to finish the authentication and get access tokens.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class AuthSession(
    authentication: SteamAuthentication,
    authenticator: IAuthenticator?,
    clientId: Long,
    requestId: ByteArray,
    allowedConfirmations: List<CAuthentication_AllowedConfirmation>,
    pollingInterval: Float,
) {

    // private val logger = LogManager.getLogger(AuthSession::class.java)

    /**
     * Unified messages class for Authentication related messages, see [Authentication].
     */
    var authentication: SteamAuthentication
        private set

    /**
     * Confirmation types that will be able to confirm the request.
     */
    var allowedConfirmations: List<CAuthentication_AllowedConfirmation>
        private set

    /**
     * Authenticator object which will be used to handle 2-factor authentication if necessary.
     */
    var authenticator: IAuthenticator?
        private set

    /**
     * Unique identifier of requestor, also used for routing, portion of QR code.
     */
    var clientID: Long
        private set

    /**
     * Unique request ID to be presented by requestor at poll time.
     */
    var requestID: ByteArray
        private set

    /**
     * Refresh interval with which requestor should call PollAuthSessionStatus.
     */
    var pollingInterval: Float
        private set

    init {
        this.allowedConfirmations = sortConfirmations(allowedConfirmations)
        this.authentication = authentication
        this.authenticator = authenticator
        this.clientID = clientId
        this.pollingInterval = pollingInterval
        this.requestID = requestId
    }

    /**
     * Blocking, compat function for Java mostly:
     * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
     *
     * @return An object containing tokens which can be used to log in to Steam.
     */
    @Throws(
        UnsupportedOperationException::class,
        AuthenticationException::class,
        NotImplementedError::class,
        NullPointerException::class,
        IllegalStateException::class,
        CancellationException::class,
    )
    fun pollingWaitForResultCompat(): AuthPollResult {
        return runBlocking { pollingWaitForResult(this) }
    }

    /**
     * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
     *
     * @return An object containing tokens which can be used to log in to Steam.
     */
    @Throws(
        AuthenticationException::class,
        CancellationException::class,
        IllegalStateException::class,
        NotImplementedError::class,
        NullPointerException::class,
        UnsupportedOperationException::class,
    )
    suspend fun pollingWaitForResult(coroutineScope: CoroutineScope): AuthPollResult {
        var pollLoop = false
        var preferredConfirmation = allowedConfirmations.firstOrNull()

        if (preferredConfirmation == null ||
            preferredConfirmation.confirmationType == EAuthSessionGuardType.k_EAuthSessionGuardType_Unknown
        ) {
            throw IllegalStateException("There are no allowed confirmations");
        }

        // If an authenticator is provided and the device confirmation is available, allow consumers to choose whether they want to
        // simply poll until confirmation is accepted, or whether they want to fall back to the next preferred confirmation type.
        if (authenticator != null &&
            preferredConfirmation.confirmationType == EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation
        ) {
            val prefersToPollForConfirmation = authenticator!!.acceptDeviceConfirmation().get()

            if (!prefersToPollForConfirmation) {
                if (allowedConfirmations.size <= 1) {
                    throw IllegalStateException(
                        "AcceptDeviceConfirmation returned false which indicates a fallback to another confirmation type, " +
                            "but there are no other confirmation types available."
                    )
                }

                preferredConfirmation = allowedConfirmations[1]
            }
        }

        when (preferredConfirmation.confirmationType) {
            // No steam guard
            EAuthSessionGuardType.k_EAuthSessionGuardType_None -> Unit
            // 2-factor code from the authenticator app or sent to an email
            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode,
            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> {
                val credentialsAuthSession = this as? CredentialsAuthSession
                    ?: throw IllegalStateException("Got ${preferredConfirmation.confirmationType} confirmation type " +
                        "in a session that is not CredentialsAuthSession.")

                if (authenticator == null) {
                    throw NullPointerException(
                        "This account requires an authenticator for login, " +
                            "but none was provided in 'AuthSessionDetails'."
                    )
                }

                val expectedInvalidCodeResult = when (preferredConfirmation.confirmationType) {
                    EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode -> EResult.InvalidLoginAuthCode
                    EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> EResult.TwoFactorCodeMismatch
                    else -> throw NotImplementedError()
                }

                var previousCodeWasIncorrect = false
                var waitingForValidCode = true

                while (waitingForValidCode) {
                    try {
                        val task = when (preferredConfirmation.confirmationType) {
                            EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode ->
                                authenticator!!.getEmailCode(preferredConfirmation.associatedMessage, previousCodeWasIncorrect).get()

                            EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode ->
                                authenticator!!.getDeviceCode(previousCodeWasIncorrect).get()

                            else -> throw NotImplementedError()
                        }

                        if (task.isNullOrEmpty()) {
                            throw IllegalStateException("No code was provided by the authenticator.")
                        }

                        credentialsAuthSession.sendSteamGuardCode(task, preferredConfirmation.confirmationType)

                        waitingForValidCode = false
                    } catch (e: AuthenticationException) {
                        if (e.result == expectedInvalidCodeResult) {
                            previousCodeWasIncorrect = true
                        }
                    }
                }
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

            else -> throw UnsupportedOperationException(
                "Unsupported confirmation type ${preferredConfirmation.confirmationType}."
            )
        }

        if (!pollLoop) {
            return pollAuthSessionStatus() ?: throw AuthenticationException("Authentication failed", EResult.Fail)
        }


        var pollResponse: AuthPollResult? = null
        while (pollResponse == null) {
            coroutineScope.ensureActive()

            pollResponse = pollAuthSessionStatus()

            delay(pollingInterval.toLong())
        }

        return pollResponse
    }

    /**
     * Polls for authentication status once. Prefer using [pollingWaitForResult] instead.
     *
     * @return An object containing tokens which can be used to log in to Steam, or null if not yet authenticated.
     * @throws AuthenticationException Thrown when polling fails.
     */
    private fun pollAuthSessionStatus(): AuthPollResult? {
        val request = CAuthentication_PollAuthSessionStatus_Request.newBuilder()
        request.clientId = clientID
        request.requestId = ByteString.copyFrom(requestID)

        val message = authentication.authenticationService.PollAuthSessionStatus(request.build()).runBlock()

        // eresult can be Expired, FileNotFound, Fail
        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to poll status", message.result)
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