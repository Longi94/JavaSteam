package in.dragonbra.javasteam.steam.handlers.steamauthentication;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface IAuthenticator {
    /**
     * This method is called when the account being logged into requires 2-factor authentication using the authenticator app.
     *
     * @param previousCodeWasIncorrect True when previously provided code was incorrect.
     * @return The 2-factor auth code used to login. This is the code that can be received from the authenticator app.
     */
    CompletableFuture<String> provideDeviceCode(boolean previousCodeWasIncorrect);

    /**
     * This method is called when the account being logged into uses Steam Guard email authentication. This code is sent to the user's email.
     *
     * @param email                    The email address that the Steam Guard email was sent to.
     * @param previousCodeWasIncorrect True when previously provided code was incorrect.
     * @return The Steam Guard auth code used to login.
     */
    CompletableFuture<String> provideEmailCode(String email, boolean previousCodeWasIncorrect);

    /**
     * This method is called when the account being logged has the Steam Mobile App and accepts authentication notification prompts.
     * This method is called when the account being logged has the Steam Mobile App and accepts authentication notification prompts.
     *
     * @return Return true to poll until the authentication is accepted, return false to fallback to entering a code.
     */
    CompletableFuture<Boolean> acceptDeviceConfirmation();
}