package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.ESessionPersistence
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_DeviceDetails
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_GetPasswordRSAPublicKey_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CAuthentication_GetPasswordRSAPublicKey_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.ETokenRenewalType
import `in`.dragonbra.javasteam.rpc.interfaces.IAuthentication
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.crypto.CryptoHelper
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.util.*
import javax.crypto.Cipher
import kotlin.coroutines.cancellation.CancellationException

/**
 * This handler is used for authenticating on Steam.
 *
 * @constructor Initializes a new instance of the [SteamAuthentication] class.
 * @param steamClient this instance will be associated with.
 */
class SteamAuthentication(private val steamClient: SteamClient) {

    companion object {
        // private val logger = LogManager.getLogger(SteamAuthentication::class.java)
    }

    internal val authenticationService: UnifiedService<IAuthentication>

    init {
        val unifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)
            ?: throw NullPointerException("Unable to get SteamUnifiedMessages handler")

        authenticationService = unifiedMessages.createService(IAuthentication::class.java)
    }

    /**
     * Gets public key for the provided account name which can be used to encrypt the account password.
     * @param accountName The account name to get RSA public key for.
     * @return The [CAuthentication_GetPasswordRSAPublicKey_Response] response.
     * @throws AuthenticationException if getting the public key failed.
     */
    @Throws(AuthenticationException::class)
    private fun getPasswordRSAPublicKey(accountName: String?): CAuthentication_GetPasswordRSAPublicKey_Response.Builder {
        val request = CAuthentication_GetPasswordRSAPublicKey_Request.newBuilder().apply {
            this.accountName = accountName
        }

        val message = authenticationService.sendMessage { api ->
            api.GetPasswordRSAPublicKey(request.build())
        }.runBlocking()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to get password public key", message.result)
        }

        return message.getDeserializedResponse(CAuthentication_GetPasswordRSAPublicKey_Response::class.java)
    }

    /**
     * Given a refresh token for a client app audience (e.g. desktop client / mobile client), generate an access token.
     * @param steamID the SteamID this token belongs to.
     * @param refreshToken the refresh token.
     * @param allowRenewal If true, allow renewing the token.
     * @return A [AccessTokenGenerateResult] containing the new token
     */
    @Throws(IllegalArgumentException::class, IllegalArgumentException::class)
    @JvmOverloads
    fun generateAccessTokenForApp(
        steamID: SteamID,
        refreshToken: String,
        allowRenewal: Boolean = false,
    ): AccessTokenGenerateResult {
        val request = CAuthentication_AccessToken_GenerateForApp_Request.newBuilder().apply {
            this.refreshToken = refreshToken
            this.steamid = steamID.convertToUInt64()

            if (allowRenewal) {
                this.renewalType = ETokenRenewalType.k_ETokenRenewalType_Allow
            }
        }

        val message = authenticationService.sendMessage { api ->
            api.GenerateAccessTokenForApp(request.build())
        }.runBlocking()

        if (message.result != EResult.OK) {
            throw IllegalArgumentException("Failed to generate token ${message.result}")
        }

        val response = message.getDeserializedResponse<CAuthentication_AccessToken_GenerateForApp_Response.Builder>(
            CAuthentication_AccessToken_GenerateForApp_Response::class.java
        )

        return AccessTokenGenerateResult(response)
    }

    /**
     * Start the authentication process using QR codes.
     * @param authSessionDetails The details to use for logging on.
     * @return [QrAuthSession]
     * @throws AuthenticationException if the session failed to start.
     */
    @Throws(AuthenticationException::class, CancellationException::class)
    fun beginAuthSessionViaQR(authSessionDetails: AuthSessionDetails): QrAuthSession {
        if (!steamClient.isConnected) {
            throw IllegalArgumentException("The SteamClient instance must be connected.")
        }

        val deviceDetails = CAuthentication_DeviceDetails.newBuilder().apply {
            this.deviceFriendlyName = authSessionDetails.deviceFriendlyName
            this.platformType = authSessionDetails.platformType
            this.osType = authSessionDetails.clientOSType.code()
        }

        val request = CAuthentication_BeginAuthSessionViaQR_Request.newBuilder().apply {
            this.websiteId = authSessionDetails.websiteID
            this.deviceDetails = deviceDetails.build()
        }

        val message = authenticationService.sendMessage { api ->
            api.BeginAuthSessionViaQR(request.build())
        }.runBlocking()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to begin QR auth session", message.result)
        }

        val response = message.getDeserializedResponse<CAuthentication_BeginAuthSessionViaQR_Response.Builder>(
            CAuthentication_BeginAuthSessionViaQR_Response::class.java
        )

        return QrAuthSession(this, authSessionDetails.authenticator, response)
    }

    /**
     * Start the authentication process by providing username and password.
     * @param authSessionDetails The details to use for logging on.
     * @return [CredentialsAuthSession]
     */
    @Throws(AuthenticationException::class)
    fun beginAuthSessionViaCredentials(authSessionDetails: AuthSessionDetails?): CredentialsAuthSession {
        if (authSessionDetails == null) {
            throw IllegalArgumentException("authSessionDetails is null")
        }

        if (authSessionDetails.username.isNullOrEmpty() || authSessionDetails.password.isNullOrEmpty()) {
            throw IllegalArgumentException(
                "BeginAuthSessionViaCredentials requires a username and password to be set in authSessionDetails."
            )
        }

        if (!steamClient.isConnected) {
            throw IllegalArgumentException("The SteamClient instance must be connected.")
        }

        // Encrypt the password
        val passwordRSAPublicKey = getPasswordRSAPublicKey(authSessionDetails.username)

        val publicModulus = BigInteger(passwordRSAPublicKey.publickeyMod, 16)
        val publicExponent = BigInteger(passwordRSAPublicKey.publickeyExp, 16)

        val rsaPublicKeySpec = RSAPublicKeySpec(publicModulus, publicExponent)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec)

        val cipher = Cipher.getInstance("RSA/None/PKCS1Padding", CryptoHelper.SEC_PROV).apply {
            init(Cipher.ENCRYPT_MODE, publicKey)
        }

        val encryptedPassword = Base64.getEncoder().encodeToString(
            cipher.doFinal(authSessionDetails.password?.toByteArray(StandardCharsets.UTF_8))
        ).dropLast(1) // Drop the "=" symbol

        val persistentSession = if (authSessionDetails.persistentSession) {
            ESessionPersistence.k_ESessionPersistence_Persistent
        } else {
            ESessionPersistence.k_ESessionPersistence_Ephemeral
        }

        // Create request
        val deviceDetails = CAuthentication_DeviceDetails.newBuilder().apply {
            this.deviceFriendlyName = authSessionDetails.deviceFriendlyName
            this.osType = authSessionDetails.clientOSType.code()
            this.platformType = authSessionDetails.platformType
        }

        val request = CAuthentication_BeginAuthSessionViaCredentials_Request.newBuilder().apply {
            this.accountName = authSessionDetails.username
            this.deviceDetails = deviceDetails.build()
            this.encryptedPassword = encryptedPassword
            this.encryptionTimestamp = passwordRSAPublicKey.timestamp
            this.persistence = persistentSession
            this.websiteId = authSessionDetails.websiteID
        }

        if (!authSessionDetails.guardData.isNullOrEmpty()) {
            request.guardData = authSessionDetails.guardData
        }

        val message = authenticationService.sendMessage { api ->
            api.BeginAuthSessionViaCredentials(request.build())
        }.runBlocking()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Authentication failed", message.result)
        }

        val response = message.getDeserializedResponse<CAuthentication_BeginAuthSessionViaCredentials_Response.Builder>(
            CAuthentication_BeginAuthSessionViaCredentials_Response::class.java
        )

        return CredentialsAuthSession(this, authSessionDetails.authenticator, response)
    }
}
