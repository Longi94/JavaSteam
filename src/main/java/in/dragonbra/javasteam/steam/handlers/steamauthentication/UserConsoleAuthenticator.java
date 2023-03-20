package in.dragonbra.javasteam.steam.handlers.steamauthentication;

import in.dragonbra.javasteam.util.Strings;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * This is a default implementation of {@link IAuthenticator} to ease of use.
 */
public class UserConsoleAuthenticator implements IAuthenticator {


    @Override
    public CompletableFuture<String> provideDeviceCode(boolean previousCodeWasIncorrect) {
        if (previousCodeWasIncorrect) {
            System.out.println("The previous 2-factor auth code you have provided is incorrect.");
        }

        String code;

        do {
            System.out.print("STEAM GUARD! Please enter your 2-factor auth code from your authenticator app: ");
            code = new Scanner(System.in).nextLine().trim();
        } while (Strings.isNullOrEmpty(code));

        return CompletableFuture.completedFuture(code);
    }

    @Override
    public CompletableFuture<String> provideEmailCode(String email, boolean previousCodeWasIncorrect) {
        if (previousCodeWasIncorrect) {
            System.out.println("The previous 2-factor auth code you have provided is incorrect.");
        }

        String code;

        do {
            System.out.print("STEAM GUARD! Please enter the auth code sent to the email at " + email + ": ");
            code = new Scanner(System.in).nextLine().trim();
        }
        while (Strings.isNullOrEmpty(code));

        return CompletableFuture.completedFuture(code);
    }

    @Override
    public CompletableFuture<Boolean> acceptDeviceConfirmation() {
        System.out.println("STEAM GUARD! Use the Steam Mobile App to confirm your sign in...");

        return CompletableFuture.completedFuture(true);
    }
}
