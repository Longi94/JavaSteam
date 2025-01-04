package in.dragonbra.javasteamsamples._023_downloadapp;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.contentdownloader.ContentDownloader;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.FreeLicenseCallback;
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

import java.io.File;

/**
 * @author Oxters
 * @since 2024-11-07
 * <p>
 * Sample 23: Download App
 * <p>
 * this sample introduces the usage of the content downloader API
 * <p>
 * content downloader lets you download an app from a Steam depot given
 * an app ID
 * <p>
 * in this case, this sample will demonstrate how to download the free game
 * called Rocky Mayhem
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleDownloadApp implements Runnable {

    private final int ROCKY_MAYHEM_APP_ID = 1303350;
    private final int ROCKY_MAYHEM_DEPOT_ID = 1303351;

    private SteamClient steamClient;

    private CallbackManager manager;
    private SteamUser steamUser;
    private SteamApps steamApps;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private final String twoFactor;

    public SampleDownloadApp(String user, String pass, String twoFactor) {
        this.user = user;
        this.pass = pass;
        this.twoFactor = twoFactor;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample23: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        String twoFactor = null;
        if (args.length == 3)
            twoFactor = args[2];
        new SampleDownloadApp(args[0], args[1], twoFactor).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);
        steamApps = steamClient.getHandler(SteamApps.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(FreeLicenseCallback.class, this::onFreeLicense);

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
        if (twoFactor != null) {
            details.setTwoFactorCode(twoFactor);
        }

        // Set LoginID to a non-zero value if you have another client connected using the same account,
        // the same private ip, and same public ip.
        details.setLoginID(149);

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");

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
            if (callback.getResult() == EResult.AccountLogonDenied) {
                // if we receive AccountLogonDenied or one of its flavors (AccountLogonDeniedNoMailSent, etc.)
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

        // now that we are logged in, we can request a free license for Rocky Mayhem
        steamApps.requestFreeLicense(ROCKY_MAYHEM_APP_ID);

    }

    private void onFreeLicense(FreeLicenseCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Failed to get a free license for Rocky Mayhem");
            steamClient.disconnect();
            return;
        }

        // we have successfully received a free license for Rocky Mayhem so now we can start the download process
        // note: it is okay to see some errors about ContentDownloader failing to download a chunk, it will retry and continue.
        new File("steamapps/staging/").mkdirs();
        var contentDownloader = new ContentDownloader(steamClient);
        contentDownloader.downloadApp(
                ROCKY_MAYHEM_APP_ID,
                ROCKY_MAYHEM_DEPOT_ID,
                "steamapps/",
                "steamapps/staging/",
                "public",
                8,
                progress -> System.out.println("Download progress: " + progress)
        ).thenAccept(success -> {
            if (success) {
                System.out.println("Download completed successfully");
            }
            steamClient.disconnect();
        });
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
