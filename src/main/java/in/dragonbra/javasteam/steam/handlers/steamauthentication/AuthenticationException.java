package in.dragonbra.javasteam.steam.handlers.steamauthentication;

import in.dragonbra.javasteam.enums.EResult;

/**
 * Thrown when {@link SteamAuthentication} fails to authenticate.
 */
public class AuthenticationException extends Exception {

    private EResult result;

    /**
     * Initializes a new instance of the {@link AuthenticationException} class.
     *
     * @param message The message that describes the error.
     * @param result  The result code that describes the error.
     */
    public AuthenticationException(String message, EResult result) {
        super(message + " with result " + result);

        this.result = result;
    }

    /**
     * @return the result of the authentication request.
     */
    public EResult getResult() {
        return result;
    }
}
