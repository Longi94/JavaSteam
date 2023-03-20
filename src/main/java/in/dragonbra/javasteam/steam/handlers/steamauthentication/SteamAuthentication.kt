package `in`.dragonbra.javasteam.steam.handlers.steamauthentication

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.ESessionPersistence
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*
import `in`.dragonbra.javasteam.rpc.service.Authentication
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.compat.Consumer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPublicKeySpec
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * This handler is used for authenticating on Steam.
 */
class SteamAuthentication : ClientMsgHandler() {

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        /* Not Used */
    }

    /**
     * Represents the details required to authenticate on Steam.
     */
    class AuthSessionDetails {
        /**
         * Gets or Sets the username.
         */
        @JvmField
        var username: String? = null

        /**
         * Gets or Sets the password.
         */
        @JvmField
        var password: String? = null

        /**
         * Gets or Sets the device name (or user agent).
         */
        var deviceFriendlyName: String?

        /**
         * Gets or sets the platform type that the login will be performed for.
         */
        @JvmField
        var platformType: EAuthTokenPlatformType? = null

        /**
         * Gets or Sets the client operating system type.
         */
        @JvmField
        var clientOSType: EOSType? = null

        /**
         * Gets or Sets the session persistence.
         */
        @JvmField
        var persistentSession: Boolean = false

        /**
         * Gets or Sets the website id that the login will be performed for.
         * Known values are "Unknown", "Client", "Mobile", "Website", "Store", "Community", "Partner".
         */
        @JvmField
        var websiteID: String? = null

        /**
         * Steam guard data for client login. Provide <see cref="AuthPollResult.NewGuardData"></see> if available.
         */
        @JvmField
        var guardData: String? = null

        /**
         * Authenticator object which will be used to handle 2-factor authentication if necessary.
         * Use <see cref="UserConsoleAuthenticator"></see> for a default implementation.
         */
        @JvmField
        var authenticator: IAuthenticator? = null

        init {
            var machineName = System.getenv("COMPUTERNAME")
            if (machineName == null) {
                machineName = System.getenv("HOSTNAME")
            }
            deviceFriendlyName = machineName
            platformType = EAuthTokenPlatformType.k_EAuthTokenPlatformType_SteamClient
        }
    }

    /**
     *
     */
    class AuthPollResult(response: CAuthentication_PollAuthSessionStatus_Response) {
        /**
         * Account name of authenticating account.
         */
        @JvmField
        var accountName: String

        /**
         * New refresh token.
         */
        @JvmField
        var refreshToken: String

        /**
         * Gets or Sets the new token subordinate to refresh_token.
         */
        @JvmField
        var accessToken: String

        /**
         * May contain remembered machine ID for future login.
         */
        @JvmField
        var newGuardData: String

        init {
            accessToken = response.accessToken
            accountName = response.accountName
            newGuardData = response.newGuardData
            refreshToken = response.refreshToken
        }
    }

    /**
     * Represents an authentication sesssion which can be used to finish the authentication and get access tokens.
     */
    open inner class AuthSession(
        var authenticationService: Authentication,
        var authenticator: IAuthenticator?, // Authenticator object which will be used to handle 2-factor authentication if necessary.
        var clientID: Long, // Unique identifier of requestor, also used for routing, portion of QR code.
        var requestID: ByteArray, // Unique request ID to be presented by requestor at poll time.
        var allowedConfirmations: List<CAuthentication_AllowedConfirmation>, // Confirmation types that will be able to confirm the request.
        var pollingInterval: Float // Refresh interval with which requestor should call PollAuthSessionStatus.
    ) {

        init {
            this.allowedConfirmations = sortConfirmations(allowedConfirmations)
        }

        /**
         * Handle any 2-factor authentication, and if necessary poll for updates until authentication succeeds.
         *
         * @return An object containing tokens which can be used to log in to Steam.
         */
        @Throws(
            IllegalArgumentException::class,
            AuthenticationException::class,
            ExecutionException::class,
            InterruptedException::class
        )
        fun startPolling(): AuthPollResult {
            var pollLoop = false
            var preferredConfirmation: CAuthentication_AllowedConfirmation? =
                allowedConfirmations.stream().findFirst().orElse(null)

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
                EAuthSessionGuardType.k_EAuthSessionGuardType_None -> {}
                EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode, EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> {
                    require(this is CredentialsAuthSession) {
                        "Got ${preferredConfirmation.confirmationType} confirmation type in a session that is not CredentialsAuthSession."
                    }

                    requireNotNull(authenticator) {
                        "This account requires an authenticator for login, but none was provided in AuthSessionDetails."
                    }

                    val expectedInvalidCodeResult: EResult = when (preferredConfirmation.confirmationType) {
                        EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode -> EResult.InvalidLoginAuthCode
                        EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode -> EResult.TwoFactorCodeMismatch
                        else -> throw IllegalArgumentException(preferredConfirmation.confirmationType.toString() + " not implemented")
                    }

                    var previousCodeWasIncorrect = false
                    var waitingForValidCode = true

                    do {
                        try {
                            val task: CompletableFuture<String> = when (preferredConfirmation.confirmationType) {
                                EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode ->
                                    authenticator!!.provideEmailCode(
                                        preferredConfirmation.associatedMessage,
                                        previousCodeWasIncorrect
                                    )

                                EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode ->
                                    authenticator!!.provideDeviceCode(previousCodeWasIncorrect)

                                else -> throw IllegalArgumentException()
                            }

                            val code = task.get()

                            require(!Strings.isNullOrEmpty(code)) { "No code was provided by the authenticator." }

                            this.sendSteamGuardCode(code, preferredConfirmation.confirmationType)

                            waitingForValidCode = false
                        } catch (e: AuthenticationException) {
                            if (e.result == expectedInvalidCodeResult) {
                                previousCodeWasIncorrect = true
                            }
                        }
                    } while (waitingForValidCode)
                }

                EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation -> pollLoop = true

//                EAuthSessionGuardType.k_EAuthSessionGuardType_EmailConfirmation -> {
//                    // TODO: what is this?
//                    pollLoop = true
//                }
//                EAuthSessionGuardType.k_EAuthSessionGuardType_MachineToken -> {
//                    // ${u.De.LOGIN_BASE_URL}jwt/checkdevice - with steam machine guard cookie set
//                    throw IllegalArgumentException("Machine token confirmation is not supported by SteamKit at the moment.");
//                }

                else -> throw IllegalArgumentException("Unsupported confirmation type " + preferredConfirmation.confirmationType + ".")
            }

            if (!pollLoop) {
                return pollAuthSessionStatus() ?: throw AuthenticationException("Authentication failed", EResult.Fail)
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
         * Polls for authentication status once. Prefer using <see cref="StartPolling"></see> instead.
         *
         * @return An object containing tokens which can be used to login to Steam, or null if not yet authenticated.
         * @throws AuthenticationException Thrown when polling fails.
         */
        @Throws(AuthenticationException::class)
        fun pollAuthSessionStatus(): AuthPollResult? {
            println("pollAuthSessionStatus")

            val request = CAuthentication_PollAuthSessionStatus_Request.newBuilder()
            request.clientId = clientID
            request.requestId = ByteString.copyFrom(requestID)

            val message: ServiceMethodResponse = runBlocking {
                authenticationService.PollAuthSessionStatus(request.build()).toAwait()
            }

            // eresult can be Expired, FileNotFound, Fail
            if (message.result != EResult.OK) {
                throw AuthenticationException("Failed to poll status", message.result)
            }

            val response: CAuthentication_PollAuthSessionStatus_Response.Builder =
                message.getDeserializedResponse(CAuthentication_PollAuthSessionStatus_Response::class.java)

            if (response.newClientId > 0) {
                clientID = response.newClientId
            }

            if (this is QrAuthSession && response.newChallengeUrl.isNotEmpty()) {
                this.challengeUrl = response.newChallengeUrl
                // ((QrAuthSession) this).getChallengeUrl().invoke();
            }

            return if (response.refreshToken.isNotEmpty()) AuthPollResult(response.build()) else null
        }
    }

    /**
     * QR code based authentication session.
     */
    inner class QrAuthSession(
        service: Authentication,
        authenticator: IAuthenticator?,
        response: CAuthentication_BeginAuthSessionViaQR_Response.Builder
    ) : AuthSession(
        authenticationService = service,
        authenticator = authenticator,
        clientID = response.clientId,
        requestID = response.requestId.toByteArray(),
        allowedConfirmations = response.allowedConfirmationsList,
        pollingInterval = response.interval
    ) {
        // URL based on client ID, which can be rendered as QR code.
        var challengeUrl: String

        // Called whenever the challenge url is refreshed by Steam.
        val challengeUrlChanged: Consumer<*>? = null

        init {
            challengeUrl = response.challengeUrl
        }
    }

    /**
     * Credentials based authentication session.
     */
    inner class CredentialsAuthSession(
        service: Authentication,
        authenticator: IAuthenticator?,
        response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder
    ) : AuthSession(
        authenticationService = service,
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

            val message: ServiceMethodResponse = runBlocking {
                authenticationService.UpdateAuthSessionWithSteamGuardCode(request.build()).toAwait()
            }

            val response: CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response.Builder =
                message.getDeserializedResponse(CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response::class.java)

            if (message.result != EResult.OK) {
                throw AuthenticationException("Failed to send steam guard code", message.result)
            }

            // response may contain agreement_session_url
            response.agreementSessionUrl // Useless, just stops `Variable 'response' is never used`
        }
    }

    /**
     * Gets public key for the provided account name which can be used to encrypt the account password.
     *
     * @param accountName           The account name to get RSA public key for.
     * @param authenticationService IAuthentication unified service.
     * @return The CAuthentication_GetPasswordRSAPublicKey_Response response.
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class)
    private fun getPasswordRSAPublicKey(
        accountName: String?,
        authenticationService: Authentication
    ): CAuthentication_GetPasswordRSAPublicKey_Response.Builder {
        val request = CAuthentication_GetPasswordRSAPublicKey_Request.newBuilder().apply {
            this.accountName = accountName
        }.build()

        val message: ServiceMethodResponse = runBlocking {
            authenticationService.GetPasswordRSAPublicKey(request).toAwait()
        }

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to get password public key", message.result)
        }

        return message.getDeserializedResponse(CAuthentication_GetPasswordRSAPublicKey_Response::class.java)
    }

    /**
     * Start the authentication process using QR codes.
     *
     * @param details The details to use for logging on.
     * @return QrAuthSession
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class)
    fun beginAuthSessionViaQR(details: AuthSessionDetails, unifiedMessages: SteamUnifiedMessages?): QrAuthSession {
        val deviceDetails = CAuthentication_DeviceDetails.newBuilder()
        deviceDetails.deviceFriendlyName = deviceDetails.deviceFriendlyName
        deviceDetails.platformType = deviceDetails.platformType
        deviceDetails.osType = details.clientOSType!!.code()

        val request = CAuthentication_BeginAuthSessionViaQR_Request.newBuilder()
        request.websiteId = details.websiteID
        request.deviceDetails = deviceDetails.build()

        val authentication = Authentication(unifiedMessages)

        val message: ServiceMethodResponse = runBlocking {
            authentication.BeginAuthSessionViaQR(request.build()).toAwait()
        }

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to begin QR auth session", message.result)
        }

        val response: CAuthentication_BeginAuthSessionViaQR_Response.Builder =
            message.getDeserializedResponse(CAuthentication_BeginAuthSessionViaQR_Response::class.java)

        return QrAuthSession(authentication, details.authenticator, response)
    }

    /**
     * Start the authentication process by providing username and password.
     *
     * @param details         The details to use for logging on.
     * @param unifiedMessages
     * @return CredentialsAuthSession
     */
    @Throws(
        AuthenticationException::class,
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun beginAuthSessionViaCredentials(
        details: AuthSessionDetails?,
        unifiedMessages: SteamUnifiedMessages
    ): CredentialsAuthSession {
        requireNotNull(details) { "details is null" }

        require(!Strings.isNullOrEmpty(details.username) || !Strings.isNullOrEmpty(details.password)) {
            "BeginAuthSessionViaCredentials requires a username and password to be set in 'details'."
        }

        val authenticationService = Authentication(unifiedMessages)

        // Encrypt the password
        val passwordRSAPublicKey = getPasswordRSAPublicKey(details.username, authenticationService)

        val mod = passwordRSAPublicKey.publickeyMod.toByteArray(StandardCharsets.UTF_8)
        val exp = passwordRSAPublicKey.publickeyExp.toByteArray(StandardCharsets.UTF_8)
        val publicKeyModulus = BigInteger(1, mod)
        val publicKeyExponent = BigInteger(1, exp)

        val rsaPublicKeySpec = RSAPublicKeySpec(publicKeyModulus, publicKeyExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(rsaPublicKeySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedPassword = cipher.doFinal(details.password!!.toByteArray(StandardCharsets.UTF_8))

        // Create request
        val request = CAuthentication_BeginAuthSessionViaCredentials_Request.newBuilder().apply {
            // Kinda Ugly
            details.guardData?.let { guardData = it }
            details.websiteID?.let { websiteId = it }
            accountName = details.username
            encryptionTimestamp = passwordRSAPublicKey.timestamp
            persistence =
                if (details.persistentSession) ESessionPersistence.k_ESessionPersistence_Persistent
                else ESessionPersistence.k_ESessionPersistence_Ephemeral
            this.encryptedPassword = Base64.getEncoder().encodeToString(encryptedPassword)
            this.deviceDetails = CAuthentication_DeviceDetails.newBuilder().apply {
                deviceFriendlyName = details.deviceFriendlyName
                osType = details.clientOSType?.code() ?: EOSType.Unknown.code()
                platformType = details.platformType
            }.build()
        }.build()

        val message: ServiceMethodResponse = runBlocking {
            authenticationService.BeginAuthSessionViaCredentials(request).toAwait()
        }

        if (message.result != EResult.OK) {
            throw AuthenticationException("Authentication failed", message.result)
        }

        val response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder =
            message.getDeserializedResponse(CAuthentication_BeginAuthSessionViaCredentials_Response::class.java)

        return CredentialsAuthSession(authenticationService, details.authenticator, response)
    }

    companion object {
        /**
         * Sort available guard confirmation methods by an order that we prefer to handle them in
         *
         * @param confirmations the list of confirmations
         * @return a sorted list of confirmations
         */
        private fun sortConfirmations(confirmations: List<CAuthentication_AllowedConfirmation>): List<CAuthentication_AllowedConfirmation> {
            val preferredConfirmationTypes: Array<EAuthSessionGuardType> = arrayOf(
                EAuthSessionGuardType.k_EAuthSessionGuardType_None,
                EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceConfirmation,
                EAuthSessionGuardType.k_EAuthSessionGuardType_DeviceCode,
                EAuthSessionGuardType.k_EAuthSessionGuardType_EmailCode,
                EAuthSessionGuardType.k_EAuthSessionGuardType_EmailConfirmation,
                EAuthSessionGuardType.k_EAuthSessionGuardType_MachineToken,
                EAuthSessionGuardType.k_EAuthSessionGuardType_Unknown
            )

            val sortOrder: Map<EAuthSessionGuardType, Int> = IntStream.range(0, preferredConfirmationTypes.size)
                .boxed()
                .collect(Collectors.toMap({ i: Int -> preferredConfirmationTypes[i] }, { i: Int -> i }))

            return confirmations.stream()
                .sorted { x: CAuthentication_AllowedConfirmation, y: CAuthentication_AllowedConfirmation ->
                    val xSortIndex = sortOrder[x.confirmationType] ?: Int.MAX_VALUE
                    val ySortIndex = sortOrder[y.confirmationType] ?: Int.MAX_VALUE
                    xSortIndex.compareTo(ySortIndex)
                }.collect(Collectors.toList())
        }
    }
}