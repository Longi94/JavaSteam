package `in`.dragonbra.javasteam.steam.authentication

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient
import `in`.dragonbra.javasteam.rpc.service.Authentication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

//region Aliases
typealias AllowedConfirmations = SteammessagesAuthSteamclient.CAuthentication_AllowedConfirmation
typealias AuthSessionStatusRequest = SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Request
typealias AuthSessionStatusResponse = SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Response
typealias AuthSessionStatusResponseBuilder = SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Response.Builder
typealias SessionGuardType = SteammessagesAuthSteamclient.EAuthSessionGuardType
//endregion

/**
 * Represents an authentication session which can be used to finish the authentication and get access tokens.
 *
 * @param authentication Unified messages class for Authentication related messages, see [Authentication].
 * @param authenticator Authenticator object which will be used to handle 2-factor authentication if necessary.
 * @param clientId Unique identifier of requestor, also used for routing, portion of QR code.
 * @param requestId Unique request ID to be presented by requestor at poll time.
 * @param allowedConfirmations Confirmation types that will be able to confirm the request.
 * @param pollingInterval Refresh interval with which requestor should call PollAuthSessionStatus.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class AuthSession(
    val authentication: SteamAuthentication,
    val authenticator: IAuthenticator?,
    var clientId: Long, // Should be 'private set'
    val requestId: ByteArray,
    var allowedConfirmations: List<AllowedConfirmations>,
    val pollingInterval: Float,
) {

    companion object {
        // private val logger = LogManager.getLogger(AuthSession::class.java)
    }

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        allowedConfirmations = sortConfirmations(allowedConfirmations)
    }

    /**
     * Blocking, compat function for Java mostly:
     * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
     *
     * @return An [AuthPollResult] containing tokens which can be used to log in to Steam.
     */
    @Throws(AuthenticationException::class)
    fun pollingWaitForResultCompat(): CompletableFuture<AuthPollResult> {
        return scope.future { pollingWaitForResult() }
    }

    /**
     * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
     * @return An [AuthPollResult] containing tokens which can be used to log in to Steam.
     */
    @Throws(AuthenticationException::class)
    suspend fun pollingWaitForResult(): AuthPollResult {
        var preferredConfirmation = allowedConfirmations.firstOrNull()
            ?: throw AuthenticationException("There are no allowed confirmations")

        if (preferredConfirmation.confirmationType == SessionGuardType.k_EAuthSessionGuardType_Unknown) {
            throw AuthenticationException("There are no allowed confirmations")
        }

        // If an authenticator is provided and the device confirmation is available, allow consumers to choose whether they want to
        // simply poll until confirmation is accepted, or whether they want to fall back to the next preferred confirmation type.
        authenticator?.let { auth ->
            if (preferredConfirmation.confirmationType == SessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation) {
                val prefersToPollForConfirmation = auth.acceptDeviceConfirmation().await()

                if (!prefersToPollForConfirmation) {
                    if (allowedConfirmations.size <= 1) {
                        throw AuthenticationException(
                            "AcceptDeviceConfirmation returned false which indicates a fallback to another " +
                                "confirmation type, but there are no other confirmation types available."
                        )
                    }

                    preferredConfirmation = allowedConfirmations[1]
                }
            }
        }

        var pollLoop = false
        when (preferredConfirmation.confirmationType) {
            SessionGuardType.k_EAuthSessionGuardType_None -> Unit // // No steam guard
            SessionGuardType.k_EAuthSessionGuardType_EmailCode,
            SessionGuardType.k_EAuthSessionGuardType_DeviceCode,
            -> {
                // 2-factor code from the authenticator app or sent to an email
                handleCodeAuth(preferredConfirmation)
            }
            SessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation -> {
                // This is a prompt that appears in the Steam mobile app
                pollLoop = true
            }
            // SessionGuardType.k_EAuthSessionGuardType_EmailConfirmation -> Unit // Unknown
            // SessionGuardType.k_EAuthSessionGuardType_MachineToken -> Unit // Unknown
            else -> throw AuthenticationException(
                "Unsupported confirmation type ${preferredConfirmation.confirmationType}."
            )
        }

        return if (!pollLoop) {
            pollAuthSessionStatus() ?: throw AuthenticationException("Authentication failed", EResult.Fail)
        } else {
            pollDeviceConfirmation()
        }
    }

    private suspend fun handleCodeAuth(preferredConfirmation: AllowedConfirmations) {
        val credentialsAuthSession = this as? CredentialsAuthSession
            ?: throw AuthenticationException(
                "Got ${preferredConfirmation.confirmationType} confirmation type in a session " +
                    "that is not CredentialsAuthSession."
            )

        if (authenticator == null) {
            throw AuthenticationException(
                "This account requires an authenticator for login, but none was provided in 'AuthSessionDetails'."
            )
        }

        val expectedInvalidCodeResult = when (preferredConfirmation.confirmationType) {
            SessionGuardType.k_EAuthSessionGuardType_EmailCode -> EResult.InvalidLoginAuthCode
            SessionGuardType.k_EAuthSessionGuardType_DeviceCode -> EResult.TwoFactorCodeMismatch
            else -> throw AuthenticationException()
        }

        var previousCodeWasIncorrect = false
        var waitingForValidCode = true

        while (waitingForValidCode) {
            try {
                val task = when (preferredConfirmation.confirmationType) {
                    SessionGuardType.k_EAuthSessionGuardType_EmailCode -> {
                        val msg = preferredConfirmation.associatedMessage
                        authenticator.getEmailCode(msg, previousCodeWasIncorrect).await()
                    }
                    SessionGuardType.k_EAuthSessionGuardType_DeviceCode -> {
                        authenticator.getDeviceCode(previousCodeWasIncorrect).await()
                    }
                    else -> throw AuthenticationException()
                }

                if (task.isNullOrEmpty()) {
                    throw AuthenticationException("No code was provided by the authenticator.")
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

    private fun pollDeviceConfirmation(): AuthPollResult {
        while (true) {
            pollAuthSessionStatus()?.let { return it }
            Thread.sleep(pollingInterval.toLong())
        }
    }

    /**
     * Polls for authentication status once. Prefer using [pollingWaitForResult] instead.
     * @return An object containing tokens which can be used to log in to Steam, or null if not yet authenticated.
     * @throws AuthenticationException Thrown when polling fails.
     */
    fun pollAuthSessionStatus(): AuthPollResult? {
        val request = AuthSessionStatusRequest.newBuilder()
        request.clientId = clientId
        request.requestId = ByteString.copyFrom(requestId)

        val message = authentication.authenticationService.pollAuthSessionStatus(request.build()).runBlock()

        // eResult can be Expired, FileNotFound, Fail
        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to poll status", message.result)
        }

        val response: AuthSessionStatusResponseBuilder =
            message.getDeserializedResponse(AuthSessionStatusResponse::class.java)

        if (response.newClientId > 0) {
            clientId = response.newClientId
        }

        handlePollAuthSessionStatusResponse(response)

        if (response.refreshToken.isNotEmpty()) {
            return AuthPollResult(response)
        }

        return null
    }

    internal open fun handlePollAuthSessionStatusResponse(response: AuthSessionStatusResponseBuilder) {
        if (response.newClientId != 0L) {
            clientId = response.newClientId
        }
    }

    /**
     * Sort available guard confirmation methods by an order that we prefer to handle them in
     * @param confirmations the list of confirmations
     * @return a sorted list of confirmations
     */
    private fun sortConfirmations(confirmations: List<AllowedConfirmations>): List<AllowedConfirmations> {
        val preferredConfirmationTypes = arrayOf(
            SessionGuardType.k_EAuthSessionGuardType_None,
            SessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation,
            SessionGuardType.k_EAuthSessionGuardType_DeviceCode,
            SessionGuardType.k_EAuthSessionGuardType_EmailCode,
            SessionGuardType.k_EAuthSessionGuardType_EmailConfirmation,
            SessionGuardType.k_EAuthSessionGuardType_MachineToken,
            SessionGuardType.k_EAuthSessionGuardType_Unknown
        )

        val sortOrder = preferredConfirmationTypes.withIndex().associate { (index, value) -> value to index }

        return confirmations.sortedBy { x ->
            sortOrder[x.confirmationType] ?: Int.MAX_VALUE
        }
    }
}
