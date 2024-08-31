package in.dragonbra.javasteamsamples._002_webcookie;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.util.concurrent.CancellationException;

/**
 * @author lossy
 * @since 2023-11-06
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleWebCookie implements Runnable {

    private SteamClient steamClient;

    private SteamAuthentication auth;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private String accessToken;

    private String refreshToken;

    public SampleWebCookie(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("SampleWebCookie: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleWebCookie(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        isRunning = true;

        System.out.println("Connecting to steam...");

        // initiate the connection
        steamClient.connect();

        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam! Logging in " + user + "...");

        AuthSessionDetails authSessionDetails = new AuthSessionDetails();
        authSessionDetails.username = user;
        authSessionDetails.password = pass;
        authSessionDetails.persistentSession = false;
        authSessionDetails.authenticator = new UserConsoleAuthenticator();

        // get the authentication handler, which used for authenticating with Steam
        auth = new SteamAuthentication(steamClient);

        try {
            CredentialsAuthSession authSession = auth.beginAuthSessionViaCredentials(authSessionDetails);

            // Note: This is blocking, it would be up to you to make it non-blocking for Java.
            // Note: Kotlin uses should use ".pollingWaitForResult()" as its a suspending function.
            AuthPollResult pollResponse = authSession.pollingWaitForResultCompat().get();

            LogOnDetails logOnDetails = new LogOnDetails();
            logOnDetails.setUsername(pollResponse.getAccountName());
            logOnDetails.setAccessToken(pollResponse.getRefreshToken());

            // Set LoginID to a non-zero value if you have another client connected using the same account,
            // the same private ip, and same public ip.
            logOnDetails.setLoginID(149);

            steamUser.logOn(logOnDetails);

            // AccessToken can be used as the steamLoginSecure cookie
            // RefreshToken is required to generate new access tokens
            accessToken = pollResponse.getAccessToken();
            refreshToken = pollResponse.getRefreshToken();
        } catch (Exception e) {
            System.err.println(e.getMessage());

            // List a couple of exceptions that could be important to handle.
            if (e instanceof AuthenticationException) {
                System.out.println("An Authentication error has occurred. " + e.getMessage());
            }

            if (e instanceof CancellationException) {
                System.out.println("An Cancellation exception was raised. Usually means a timeout occurred. " + e.getMessage());
            }
        }
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");

        isRunning = false;
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to login to steam: " + callback.getResult() + " / " + callback.getExtendedResult());

            isRunning = false;

            return;
        }

        System.out.println("Successfully logged on!");

        // This is how you concatenate the cookie, you can set it on the Steam domains, and it should work
        // but actual usage of this will be left as an exercise for the reader
        @SuppressWarnings("unused")
        String steamLoginSecure = callback.getClientSteamID().convertToUInt64() + "||" + accessToken;

        // The access token expires in 24 hours (at the time of writing) so you will have to renew it.
        // Parse this token with a JWT library to get the expiration date and set up a timer to renew it.
        // To renew you will have to call this:
        // When allowRenewal is set to true, Steam may return new RefreshToken
        AccessTokenGenerateResult newTokens = auth.generateAccessTokenForApp(callback.getClientSteamID(), refreshToken, false);

        accessToken = newTokens.getAccessToken();

        if (!Strings.isNullOrEmpty(newTokens.getRefreshToken())) {
            refreshToken = newTokens.getRefreshToken();
        }

        // Do not forget to update steamLoginSecure with the new accessToken!

        // for this sample we'll just log off
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
