package in.dragonbra.javasteamsamples._000_authentication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * @author lossy
 * @since 2023-03-19
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleLogonAuthentication implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private List<Closeable> subscriptions;

    private String previouslyStoredGuardData; // For the sake of this sample, we do not persist guard data

    public SampleLogonAuthentication(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample 000: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleLogonAuthentication(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // If any configuration needs to be set; such as connection protocol api key, etc., you can configure it like so.
        // var config = SteamConfiguration.create(config -> {
        //    config.withProtocolTypes(ProtocolTypes.WEB_SOCKET);
        // });

        // You can also create custom connection classes if you have specific networking requirements.
        // var config = SteamConfiguration.create(builder -> {
        //     builder.withProtocolTypes(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP)); // Declare desired protocol types.
        //     IConnectionFactory connectionFactory = (configuration, protocol) -> {
        //         if (protocol.contains(ProtocolTypes.TCP)) {
        //             return new CustomTCPConnection();
        //         } else if (protocol.contains(ProtocolTypes.UDP)) {
        //             // We ask for TCP and UDP above, so this condition should handle UDP.
        //             return CustomUDPConnection();
        //         } else {
        //             // Fallback: 'thenResolve` will fallback to default connection types.
        //             return null;
        //         }
        //     };
        //     builder.withConnectionFactory(connectionFactory.thenResolve(IConnectionFactory.DEFAULT));
        // });

        // create our steamclient instance with custom configuration.
        // steamClient = new SteamClient(config);

        // create our steamclient instance using default configuration
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // The callbacks are a closeable, and to properly fix
        // "'Closeable' used without 'try'-with-resources statement", they should be closed once done.
        // Usually putting them in a list and close each of them once the client is finished is recommended.
        subscriptions = new ArrayList<>();

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        subscriptions.add(manager.subscribe(ConnectedCallback.class, this::onConnected));
        subscriptions.add(manager.subscribe(DisconnectedCallback.class, this::onDisconnected));
        subscriptions.add(manager.subscribe(LoggedOnCallback.class, this::onLoggedOn));
        subscriptions.add(manager.subscribe(LoggedOffCallback.class, this::onLoggedOff));

        isRunning = true;

        System.out.println("Connecting to steam...");

        // initiate the connection
        steamClient.connect();

        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L);
        }

        // Close the subscriptions when done.
        System.out.println("Closing " + subscriptions.size() + " callbacks");
        for (var subscription : subscriptions) {
            try {
                subscription.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a callback.");
            }
        }
    }

    @SuppressWarnings("DanglingJavadoc")
    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam! Logging in " + user + "...");

        var shouldRememberPassword = false;

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = user;
        authDetails.password = pass;
        authDetails.persistentSession = shouldRememberPassword;

        // See NewGuardData comment below.
        authDetails.guardData = previouslyStoredGuardData;

        /**
         * {@link UserConsoleAuthenticator} is the default authenticator implementation provided by JavaSteam
         * for ease of use which blocks the thread and asks for user input to enter the code.
         * However, if you require special handling (e.g. you have the TOTP secret and can generate codes on the fly),
         * you can implement your own {@link IAuthenticator}.
         */
        authDetails.authenticator = new UserConsoleAuthenticator();

        try {
            // Begin authenticating via credentials.
            var authSession = steamClient.getAuthentication().beginAuthSessionViaCredentials(authDetails).get();

            // Note: This is blocking, it would be up to you to make it non-blocking for Java.
            // Note: Kotlin uses should use ".pollingWaitForResult()" as its a suspending function.
            AuthPollResult pollResponse = authSession.pollingWaitForResult().get();

            if (pollResponse.getNewGuardData() != null) {
                // When using certain two factor methods (such as email 2fa), guard data may be provided by Steam
                // for use in future authentication sessions to avoid triggering 2FA again (this works similarly to the old sentry file system).
                // Do note that this guard data is also a JWT token and has an expiration date.
                previouslyStoredGuardData = pollResponse.getNewGuardData();
            }

            // Logon to Steam with the access token we have received
            // Note that we are using RefreshToken for logging on here
            LogOnDetails details = new LogOnDetails();
            details.setUsername(pollResponse.getAccountName());
            details.setAccessToken(pollResponse.getRefreshToken());

            // Set LoginID to a non-zero value if you have another client connected using the same account,
            // the same private ip, and same public ip.
            details.setLoginID(149);

            steamUser.logOn(details);

            // This is not required, but it is possible to parse the JWT access token to see the scope and expiration date.
            // parseJsonWebToken(pollResponse.accessToken, "AccessToken");
            // parseJsonWebToken(pollResponse.refreshToken, "RefreshToken");
        } catch (Exception e) {
            // List a couple of exceptions that could be important to handle.
            if (e instanceof AuthenticationException) {
                System.err.println("An Authentication error has occurred. " + e.getMessage());
            } else if (e instanceof CancellationException) {
                System.err.println("An Cancellation exception was raised. Usually means a timeout occurred. " + e.getMessage());
            } else {
                System.err.println("An error occurred:" + e.getMessage());
            }

            steamUser.logOff();
        }
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam. User initialized: " + callback.isUserInitiated());

        // If the disconnection was not user initiated, we will retry connecting to steam again after a short delay.
        if (callback.isUserInitiated()) {
            isRunning = false;
        } else {
            try {
                Thread.sleep(2000L);
                steamClient.connect();
            } catch (InterruptedException e) {
                System.err.println("An Interrupted exception occurred. " + e.getMessage());
            }
        }
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());

            isRunning = false;
            return;
        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam

        // for this sample we'll just log off
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }


    @SuppressWarnings("unused")
    private void parseJsonWebToken(String token, String name) {
        String[] tokenComponents = token.split("\\.");

        // Fix up base64url to normal base64
        String base64 = tokenComponents[1].replace('-', '+').replace('_', '/');

        if (base64.length() % 4 != 0) {
            base64 += new String(new char[4 - base64.length() % 4]).replace('\0', '=');
        }

        byte[] payloadBytes = Base64.getDecoder().decode(base64);

        // Payload can be parsed as JSON, and then fields such expiration date, scope, etc can be accessed
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement payload = JsonParser.parseString(new String(payloadBytes));
        String formatted = gson.toJson(payload);

        // For brevity, we will simply output formatted json to console
        System.out.println(name + ": " + formatted);
        System.out.println();
    }

}
