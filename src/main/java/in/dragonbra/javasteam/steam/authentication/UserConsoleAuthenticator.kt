package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.util.Strings
import java.util.concurrent.CompletableFuture

/**
 * This is a default implementation of [IAuthenticator] to ease of use.
 * This implementation will prompt user to enter 2-factor authentication codes in the console.
 * or inform to use the Mobile App to approve sign in.
 *
 * Note: This can be replaced with your own class using [IAuthenticator]
 */
class UserConsoleAuthenticator : IAuthenticator {

    override fun getDeviceCode(previousCodeWasIncorrect: Boolean): CompletableFuture<String> {
        if (previousCodeWasIncorrect) {
            println("The previous 2-factor auth code you have provided is incorrect.")
        }

        var code: String
        do {
            print("STEAM GUARD! Please enter your 2-factor auth code from your authenticator app: ")
            code = readln().trim()
        } while (Strings.isNullOrEmpty(code))

        return CompletableFuture.completedFuture(code)
    }

    override fun getEmailCode(email: String?, previousCodeWasIncorrect: Boolean): CompletableFuture<String> {
        if (previousCodeWasIncorrect) {
            println("The previous 2-factor auth code you have provided is incorrect.")
        }

        var code: String
        do {
            print("STEAM GUARD! Please enter the auth code sent to the email at $email: ")
            code = readln().trim()
        } while (Strings.isNullOrEmpty(code))

        return CompletableFuture.completedFuture(code)
    }

    override fun acceptDeviceConfirmation(): CompletableFuture<Boolean> {
        println("STEAM GUARD! Use the Steam Mobile App to confirm your sign in...")

        return CompletableFuture.completedFuture(true)
    }
}
