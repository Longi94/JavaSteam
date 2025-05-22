package in.dragonbra.javasteamsamples._014_steammatchmaking;

import in.dragonbra.javasteam.enums.ELobbyComparison;
import in.dragonbra.javasteam.enums.ELobbyDistanceFilter;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steammatchmaking.*;
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
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * @author lossy
 * @since 2025-05-21
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleSteamMatchmaking implements Runnable {

    private final Integer appID = 480; // Team Fortress 2

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private SteamMatchmaking steamMatchmaking;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private List<Closeable> subscriptions;

    public SampleSteamMatchmaking(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample1: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleSteamMatchmaking(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // create our steamclient instance using default configuration
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // get the steammatchmaking handler.
        steamMatchmaking = steamClient.getHandler(SteamMatchmaking.class);

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

        try {
            var filters = List.of(
                    new DistanceFilter(ELobbyDistanceFilter.Worldwide),
                    new StringFilter("CONMETHOD", "P2P", ELobbyComparison.Equal)
            );
            var lobbyListCallback = steamMatchmaking.getLobbyList(appID, filters, 20).toFuture().get();

            System.out.println("App ID: " + lobbyListCallback.getAppID());
            System.out.println("Result: " + lobbyListCallback.getResult());
            System.out.println("Lobby Size: " + lobbyListCallback.getLobbies().size());
            lobbyListCallback.getLobbies().forEach(lobby -> {
                System.out.println("\tsteamID: " + lobby.getSteamID().convertToUInt64());
                System.out.println("\tlobbyType: " + lobby.getLobbyType());
                System.out.println("\tlobbyFlags: " + lobby.getLobbyFlags());
                System.out.println("\townerSteamID: " + lobby.getOwnerSteamID());

                System.out.println("\tMetadata:");
                lobby.getMetadata().forEach((k, v) -> System.out.println("\t\tkey: " + k + " value: " + v));

                System.out.println("\tmaxMembers: " + lobby.getMaxMembers());
                System.out.println("\tnumMembers: " + lobby.getNumMembers());

                System.out.println("\tMembers:");
                lobby.getMembers().forEach(member -> {
                    System.out.println("\t\tsteamID: " + member.getSteamID().convertToUInt64());
                    System.out.println("\t\tpersonaName: " + member.getPersonaName());
                    System.out.println("\t\tMember Metadata:");
                    member.getMetadata().forEach((k, v) ->
                            System.out.println("\t\t\tkey: " + k + " value: " + v));
                });

                System.out.println("\tdistance: " + lobby.getDistance());
                System.out.println("\tweight: " + lobby.getWeight());
                System.out.println("\n");
            });


        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            steamUser.logOff();
        }
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
