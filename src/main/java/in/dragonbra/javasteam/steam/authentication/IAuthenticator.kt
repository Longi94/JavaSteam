package `in`.dragonbra.javasteam.steam.authentication

import java.util.concurrent.CompletableFuture

/**
 * Represents an authenticator to be used with [SteamAuthentication].
 */
interface IAuthenticator {
    /**
     * This method is called when the account being logged into requires 2-factor authentication using the authenticator app.
     *
     * @param previousCodeWasIncorrect True when previously provided code was incorrect.
     * @return The 2-factor auth code used to log in. This is the code that can be received from the authenticator app.
     */
    fun getDeviceCode(previousCodeWasIncorrect: Boolean): CompletableFuture<String>

    /**
     * This method is called when the account being logged into uses Steam Guard email authentication. This code is sent to the user's email.
     *
     * @param email                    The email address that the Steam Guard email was sent to.
     * @param previousCodeWasIncorrect True when previously provided code was incorrect.
     * @return The Steam Guard auth code used to log in.
     */
    fun getEmailCode(email: String?, previousCodeWasIncorrect: Boolean): CompletableFuture<String>

    /**
     * This method is called when the account being logged has the Steam Mobile App and accepts authentication notification prompts.
     *
     * @return Return true to poll until the authentication is accepted, return false to fall back to entering a code.
     */
    fun acceptDeviceConfirmation(): CompletableFuture<Boolean>
}
