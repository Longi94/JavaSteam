package in.dragonbra.javasteamsamples._021_webapi;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.steam.webapi.WebAPI;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//
// Sample 6: WebAPI
//
// this sample will give an example of how the WebAPI utilities can be used to
// interact with the Steam Web APIs
//
// the Steam Web APIs are structured as a set of "interfaces" with methods,
// similar to classes in OO languages.
// as such, the API for interacting with the WebAPI follows a similar methodology

/**
 * @author lngtr
 * @since 2021-10-11
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleWebApi implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    public SampleWebApi(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample 021: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleWebApi(args[0], args[1]).run();
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

        LogOnDetails details = new LogOnDetails();
        details.setUsername(user);
        details.setPassword(pass);

        // Set LoginID to a non-zero value if you have another client connected using the same account,
        // the same private ip, and same public ip.
        details.setLoginID(149);

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");

        isRunning = false;
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            if (callback.getResult() == EResult.AccountLogonDenied) {
                // if we recieve AccountLogonDenied or one of its flavors (AccountLogonDeniedNoMailSent, etc.)
                // then the account we're logging into is SteamGuard protected
                // see sample 5 for how SteamGuard can be handled

                System.out.println("Unable to logon to Steam: This account is SteamGuard protected.");

                isRunning = false;
                return;
            }

            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());
            isRunning = false;
            return;

        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam

        WebAPI api = steamClient.getConfiguration().getWebAPI("ISteamNews");

        try {
            Map<String, String> args = new HashMap<>();
            args.put("appid", "440");

            KeyValue result = api.call("GetNewsForApp", 2, args);

            printKeyValue(result, 1);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        // for this sample we'll just log off
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    // Recursively print out child KeyValues.
    private void printKeyValue(KeyValue keyValue, int depth) {
        String spacePadding = String.join("", Collections.nCopies(depth, "    "));

        if (keyValue.getChildren().isEmpty()) {
            System.out.println(spacePadding + keyValue.getName() + ": " + keyValue.getValue());
        } else {
            System.out.println(spacePadding + keyValue.getName() + ":");
            for (KeyValue child : keyValue.getChildren()) {
                printKeyValue(child, depth + 1);
            }
        }
    }
}
