package in.dragonbra.javasteamsamples._022_pics;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamapps.PICSRequest;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSChangesCallback;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.util.concurrent.CancellationException;

@SuppressWarnings("FieldCanBeLocal")
public class SamplePics implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamApps steamApps;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private String previouslyStoredGuardData; // For the sake of this sample, we do not persist guard data

    public SamplePics(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample1: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SamplePics(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // // If any configuration needs to be set; such as connection protocol api key, etc., you can configure it like so.
        // var configuration = SteamConfiguration.create(config -> {
        //    config.withProtocolTypes(ProtocolTypes.WEB_SOCKET);
        // });
        // // create our steamclient instance with custom configuration.
        // steamClient = new SteamClient(configuration);

        // create our steamclient instance using default configuration
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // get the steamuser handler, which is used for interacting with apps and packages.
        steamApps = steamClient.getHandler(SteamApps.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(PICSChangesCallback.class, this::onPicsChanges);
        manager.subscribe(PICSProductInfoCallback.class, this::onPicsProduct);

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
            var authSession = steamClient.getAuthentication().beginAuthSessionViaCredentials(authDetails);

            // Note: This is blocking, it would be up to you to make it non-blocking for Java.
            // Note: Kotlin uses should use ".pollingWaitForResult()" as its a suspending function.
            AuthPollResult pollResponse = authSession.pollingWaitForResultCompat().get();

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
        steamApps.picsGetProductInfo(new PICSRequest(553850), null); // We'll use HellDivers 2 since it has "tm" in the title

        // steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    private void onPicsChanges(PICSChangesCallback callback) {
        System.out.println("TODO");
    }

    private void onPicsProduct(PICSProductInfoCallback callback) {
        System.out.println("[PICSProductInfoCallback] apps: ");
        for (var app : callback.getApps().entrySet()) {
            printKeyValue(app.getValue().getKeyValues(), 1);
        }
    }

    private static void printKeyValue(KeyValue keyvalue, int depth) {
        if (keyvalue.getChildren().isEmpty())
            System.out.println("[PICSProductInfoCallback] " + " ".repeat(depth * 4) + " " + keyvalue.getName() + ": " + keyvalue.getValue());
        else {
            System.out.println("[PICSProductInfoCallback] " + " ".repeat(depth * 4) + " " + keyvalue.getName() + ":");
            for (KeyValue child : keyvalue.getChildren())
                printKeyValue(child, depth + 1);
        }
    }
}
