package in.dragonbra.javasteamsamples._3debuglog;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.util.log.LogListener;
import in.dragonbra.javasteam.util.log.LogManager;

//
// Sample 3: DebugLog
//
// sometimes is may be necessary to peek under the hood of SteamKit2
// to debug or diagnose some issues
//
// to help with this, SK2 includes a component named the DebugLog
//
// internal SK2 components will occasionally make use of the DebugLog
// to share diagnostic information
//
// in order to use the DebugLog, a listener must first be registered with it
//
// by default, SK2 does not install any listeners, user code must install one
//
// additionally, the DebugLog is disabled by default in release builds
// but it may be enabled with the DebugLog.Enabled member
//
// you'll note that while this sample project is relatively similar to
// Sample 1, the console output becomes very verbose
//

/**
 * @author lngtr
 * @since 2021-10-11
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleDebugLog implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    // define our debuglog listener
    static class MyListener implements LogListener {

        // this function will be called when internal steamkit components write to the debuglog
        @Override
        public void onLog(Class<?> clazz, String message, Throwable throwable) {
            // for this example, we'll print the output to the console
            System.out.println("MyListener - " + clazz.getName() + ": " + message);
        }

        @Override
        public void onError(Class clazz, String message, Throwable throwable) {
            // for this example, we'll print errors the output to the console
            System.err.println("MyListener - " + clazz.getName() + ": " + message);
        }
    }

    public SampleDebugLog(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample3: No username and password specified!");
            return;
        }

        // JavaSteam already has one built in for convenience.
        // LogManager.addListener(new DefaultLogListener());

        LogManager.addListener(new MyListener());

        new SampleDebugLog(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClient();

        // uncomment this if you'd like to dump raw sent and received packets
        // that can be opened for analysis in NetHookAnalyzer
        // NOTE: dumps may contain sensitive data (such as your Steam password)
        // steamClient.setDebugNetworkListener(new NetHookNetworkListener());

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
                // if we receive AccountLogonDenied or one of it's flavors (AccountLogonDeniedNoMailSent, etc)
                // then the account we're logging into is SteamGuard protected
                // see sample 5 for how SteamGuard can be handled
                System.out.println("Unable to logon to Steam: This account is SteamGuard protected.");

                isRunning = false;
                return;
            }

            System.out.println("Unable to logon to Steam: " + callback.getResult());

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
