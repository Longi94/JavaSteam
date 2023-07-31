package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.ESessionPersistence
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*
import `in`.dragonbra.javasteam.rpc.service.Authentication
import `in`.dragonbra.javasteam.steam.authentication.*
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
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
 * Initializes a new instance of the [SteamAuthentication] class.
 *
 * @param steamClient this instance will be associated with.
 * @param unifiedMessages the unified messages service.
 */
class SteamAuthentication(private val steamClient: SteamClient, unifiedMessages: SteamUnifiedMessages) {

    // private val logger = LogManager.getLogger(SteamAuthentication::class.java)

    internal val authenticationService: Authentication = Authentication(unifiedMessages)

    /**
     * Gets public key for the provided account name which can be used to encrypt the account password.
     *
     * @param accountName           The account name to get RSA public key for.
     * @return The CAuthentication_GetPasswordRSAPublicKey_Response response.
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class)
    private fun getPasswordRSAPublicKey(accountName: String?): CAuthentication_GetPasswordRSAPublicKey_Response.Builder {
        val request = CAuthentication_GetPasswordRSAPublicKey_Request.newBuilder()
        request.accountName = accountName

        val message = authenticationService.GetPasswordRSAPublicKey(request.build()).runBlock()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to get password public key", message.result)
        }

        return message.getDeserializedResponse(CAuthentication_GetPasswordRSAPublicKey_Response::class.java)
    }

    /**
     * Start the authentication process using QR codes.
     *
     * @param authSessionDetails The details to use for logging on.
     * @return QrAuthSession
     * @throws AuthenticationException .
     */
    @Throws(AuthenticationException::class, CancellationException::class)
    fun beginAuthSessionViaQR(authSessionDetails: AuthSessionDetails): QrAuthSession {
        if (!steamClient.isConnected) {
            throw IllegalArgumentException("The SteamClient instance must be connected.")
        }

        val deviceDetails = CAuthentication_DeviceDetails.newBuilder()
        deviceDetails.deviceFriendlyName = authSessionDetails.deviceFriendlyName
        deviceDetails.platformType = authSessionDetails.platformType
        deviceDetails.osType = authSessionDetails.clientOSType.code()

        val request = CAuthentication_BeginAuthSessionViaQR_Request.newBuilder()
        request.websiteId = authSessionDetails.websiteID
        request.deviceDetails = deviceDetails.build()

        val message = authenticationService.BeginAuthSessionViaQR(request.build()).runBlock()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Failed to begin QR auth session", message.result)
        }

        val response: CAuthentication_BeginAuthSessionViaQR_Response.Builder =
            message.getDeserializedResponse(CAuthentication_BeginAuthSessionViaQR_Response::class.java)

        return QrAuthSession(this, authSessionDetails.authenticator, response)
    }

    /**
     * Start the authentication process by providing username and password.
     *
     * @param authSessionDetails The details to use for logging on.
     * @return CredentialsAuthSession
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

        val cipher = Cipher.getInstance("RSA/None/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedPassword = Base64.getEncoder().encodeToString(
            cipher.doFinal(authSessionDetails.password?.toByteArray(StandardCharsets.UTF_8))
        ).dropLast(1) // Drop the "=" symbol

        val persistentSession = if (authSessionDetails.persistentSession) {
            ESessionPersistence.k_ESessionPersistence_Persistent
        } else {
            ESessionPersistence.k_ESessionPersistence_Ephemeral
        }

        // Create request
        val deviceDetails = CAuthentication_DeviceDetails.newBuilder()
        deviceDetails.deviceFriendlyName = authSessionDetails.deviceFriendlyName
        deviceDetails.osType = authSessionDetails.clientOSType.code()
        deviceDetails.platformType = authSessionDetails.platformType

        val request = CAuthentication_BeginAuthSessionViaCredentials_Request.newBuilder()
        request.accountName = authSessionDetails.username
        request.deviceDetails = deviceDetails.build()
        request.encryptedPassword = encryptedPassword
        request.encryptionTimestamp = passwordRSAPublicKey.timestamp
        request.persistence = persistentSession
        request.websiteId = authSessionDetails.websiteID

        if (!authSessionDetails.guardData.isNullOrEmpty()) {
            request.guardData = authSessionDetails.guardData
        }

        val message = authenticationService.BeginAuthSessionViaCredentials(request.build()).runBlock()

        if (message.result != EResult.OK) {
            throw AuthenticationException("Authentication failed", message.result)
        }

        val response: CAuthentication_BeginAuthSessionViaCredentials_Response.Builder =
            message.getDeserializedResponse(CAuthentication_BeginAuthSessionViaCredentials_Response::class.java)

        return CredentialsAuthSession(this, authSessionDetails.authenticator, response)
    }
}