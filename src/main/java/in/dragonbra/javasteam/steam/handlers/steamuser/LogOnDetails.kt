package `in`.dragonbra.javasteam.steam.handlers.steamuser

import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EUIMode
import `in`.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import `in`.dragonbra.javasteam.steam.authentication.SteamAuthentication
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.HardwareUtils
import `in`.dragonbra.javasteam.util.Utils

/**
 * Represents the details required to log into Steam3 as a user.
 *
 * @param username Gets or sets the username.
 * @param password Gets or sets the password.
 * @param cellID Gets or sets the CellID.
 * @param loginID Gets or sets the LoginID. This number is used for identifying logon session.
 *  The purpose of this field is to allow multiple sessions to the same steam account from the same machine.
 *  This is because Steam Network doesn't allow more than one session with the same LoginID to access given account at the same time from the same public IP.
 *  If you want to establish more than one active session to given account, you must make sure that every session (to that account) from the same public IP has a unique LoginID.
 *  By default, LoginID is automatically generated based on machine's primary bind address, which is the same for all sessions.
 *  Null value will cause this property to be automatically generated based on default behaviour.
 *  If in doubt, set this property to null.
 * @param authCode Gets or sets the Steam Guard auth code used to log in. This is the code sent to the user's email.
 * @param twoFactorCode Gets or sets the 2-factor auth code used to log in. This is the code that can be received from the authenticator apps.
 * @param shouldRememberPassword Gets or sets the 'Should Remember Password' flag. This is used in combination with the [accessToken] for password-less login. Set this to true when [AuthSessionDetails.persistentSession] is set to true.
 * @param accessToken Gets or sets the Refresh token used to log in. This a token that has been provided after a successful login using [SteamAuthentication].
 * @param accountInstance Gets or sets the account instance. 1 for the PC instance or 2 for the Console (PS3) instance. See [SteamID.DESKTOP_INSTANCE] and [SteamID.CONSOLE_INSTANCE]
 * @param accountID Gets or sets the account ID used for connecting clients when using the Console instance.
 * @param requestSteam2Ticket Gets or sets a value indicating whether to request the Steam2 ticket. This is an optional request only needed for Steam2 content downloads.
 * @param clientOSType Gets or sets the client operating system type.
 * @param clientLanguage Gets or sets the client language.
 * @param machineName Gets or sets the machine name.
 * @param chatMode Gets or sets the chat mode.
 * @param uiMode Gets or sets the ui mode.
 * @param isSteamDeck Gets or sets whether this is Steam Deck login.
 */
data class LogOnDetails(
    var username: String = "",
    var password: String? = null,
    var cellID: Int? = null,
    var loginID: Int? = null,
    var authCode: String? = null,
    var twoFactorCode: String? = null,
    var shouldRememberPassword: Boolean = false,
    var accessToken: String? = null, // This is actually your refresh token.
    var accountInstance: Long = SteamID.DESKTOP_INSTANCE,
    var accountID: Long = 0L,
    var requestSteam2Ticket: Boolean = false,
    var clientOSType: EOSType = Utils.getOSType(),
    var clientLanguage: String = "english",
    var machineName: String = HardwareUtils.getMachineName(true),
    var chatMode: ChatMode = ChatMode.DEFAULT,
    var uiMode: EUIMode = EUIMode.Unknown,
    var isSteamDeck: Boolean = false,
)
