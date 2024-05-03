package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient
import `in`.dragonbra.javasteam.util.Utils

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
     * Gets or Sets the device name (or user agent). By default, "<DeviceName>(JavaSteam)" will be used.
     */
    var deviceFriendlyName: String?

    /**
     * Gets or sets the platform type that the login will be performed for.
     */
    @JvmField
    var platformType: SteammessagesAuthSteamclient.EAuthTokenPlatformType =
        SteammessagesAuthSteamclient.EAuthTokenPlatformType.k_EAuthTokenPlatformType_SteamClient

    /**
     * Gets or Sets the client operating system type.
     */
    @JvmField
    var clientOSType: EOSType = Utils.getOSType()

    /**
     * Gets or Sets the session persistence.
     */
    @JvmField
    var persistentSession: Boolean = false

    /**
     * Gets or Sets the website id that the login will be performed for.
     * Known values are "Unknown", "Client", "Mobile", "Website", "Store", "Community", "Partner", "SteamStats".
     */
    @JvmField
    var websiteID: String = "Client"

    /**
     * Steam guard data for client login. Provide [AuthPollResult.newGuardData] if available.
     */
    @JvmField
    var guardData: String? = null

    /**
     * Authenticator object which will be used to handle 2-factor authentication if necessary.
     * Use [UserConsoleAuthenticator] for a default implementation.
     */
    @JvmField
    var authenticator: IAuthenticator? = null

    init {
        val machineName = System.getenv("COMPUTERNAME") ?: System.getenv("HOSTNAME")
        deviceFriendlyName = "$machineName (JavaSteam)"
    }
}
