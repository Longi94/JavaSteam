package in.dragonbra.javasteamsamples._004_legacylogin;

import in.dragonbra.javasteam.enums.EResult;
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

//
// Sample 1: Logon
//
// the first act of business before being able to use steamkit2's features is to
// log on to the steam network
//
// interaction with steamkit is done through client message handlers and the results
// come back through a callback queue controlled by a steamclient instance
//
// your code must create a CallbackMgr, and instances of Callback<T>. Callback<T> maps a specific
// callback type to a function, whilst CallbackMgr routes the callback objects to the functions that
// you have specified. a Callback<T> is bound to a specific callback manager.
//
// WARNING!
// This the old login flow, we keep this sample around because it still currently works
// for simple cases where you do not need to remember password.
// See SampleLogonAuthentication for the new flow.
//

/**
 * @author lngtr
 * @since 2018-02-23
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleLogon implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    public SampleLogon(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample1: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleLogon(args[0], args[1]).run();
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

        // for this sample we'll just log off
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
