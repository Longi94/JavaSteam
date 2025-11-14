package in.dragonbra.javasteamsamples._023_downloadapp;

import in.dragonbra.javasteam.depotdownloader.DepotDownloader;
import in.dragonbra.javasteam.depotdownloader.IDownloadListener;
import in.dragonbra.javasteam.depotdownloader.data.*;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.AuthPollResult;
import in.dragonbra.javasteam.steam.authentication.AuthSessionDetails;
import in.dragonbra.javasteam.steam.authentication.AuthenticationException;
import in.dragonbra.javasteam.steam.authentication.UserConsoleAuthenticator;
import in.dragonbra.javasteam.steam.handlers.steamapps.License;
import in.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback;
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
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * @author Oxters
 * @since 2024-11-07
 * <p>
 * Sample 23: Download App
 * <p>
 * this sample introduces the usage of the content downloader API
 * <p>
 * content downloader lets you download an app, pub file, or ugc item given some parameters.
 * <p>
 * in this case, this sample will ask which game app id you'd like to download.
 * You can find the app id of a game by the url of the store page.
 * For example "store.steampowered.com/app/1303350/Rocky_Mayhem/", where 1303350 is the app id.
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleDownloadApp implements Runnable, IDownloadListener {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private List<Closeable> subscriptions;

    private List<License> licenseList;

    public SampleDownloadApp(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample 023: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleDownloadApp(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // Most everything has been described in earlier samples.
        // Anything pertaining to this sample will be commented.

        steamClient = new SteamClient();

        manager = new CallbackManager(steamClient);

        steamUser = steamClient.getHandler(SteamUser.class);

        subscriptions = new ArrayList<>();

        subscriptions.add(manager.subscribe(ConnectedCallback.class, this::onConnected));
        subscriptions.add(manager.subscribe(DisconnectedCallback.class, this::onDisconnected));
        subscriptions.add(manager.subscribe(LoggedOnCallback.class, this::onLoggedOn));
        subscriptions.add(manager.subscribe(LoggedOffCallback.class, this::onLoggedOff));
        subscriptions.add(manager.subscribe(LicenseListCallback.class, this::onLicenseList));

        isRunning = true;

        System.out.println("Connecting to steam...");

        steamClient.connect();

        while (isRunning) {
            manager.runWaitCallbacks(1000L);
        }

        System.out.println("Closing " + subscriptions.size() + " subscriptions.");
        for (var subscription : subscriptions) {
            try {
                subscription.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a callback.");
            }
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam! Logging in " + user + "...");

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = user;
        authDetails.password = pass;
        authDetails.deviceFriendlyName = "JavaSteam - Sample 023";
        authDetails.persistentSession = true;

        authDetails.authenticator = new UserConsoleAuthenticator();

        try {
            var path = Paths.get("refreshtoken.txt");

            String accountName;
            String refreshToken;
            if (!Files.exists(path)) {
                System.out.println("No existing refresh token found. Beginning Authentication");

                var authSession = steamClient.getAuthentication().beginAuthSessionViaCredentials(authDetails).get();

                AuthPollResult pollResponse = authSession.pollingWaitForResult().get();

                accountName = pollResponse.getAccountName();
                refreshToken = pollResponse.getRefreshToken();

                // Save our refresh token for automatic login on next sample run.
                Files.writeString(path, pollResponse.getRefreshToken());
            } else {
                System.out.println("Existing refresh token found");
                var token = Files.readString(path);

                accountName = user;
                refreshToken = token;
            }

            LogOnDetails details = new LogOnDetails();
            details.setUsername(accountName);
            details.setAccessToken(refreshToken);
            details.setShouldRememberPassword(true);

            details.setLoginID(149);

            System.out.println("Logging in...");

            steamUser.logOn(details);
        } catch (Exception e) {
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
        System.out.println("Disconnected from Steam, UserInitiated: " + callback.isUserInitiated());

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

        // The sample continues in onLicenseList
    }

    private void onLicenseList(LicenseListCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Failed to obtain licenses the account owns.");
            steamClient.disconnect();
            return;
        }

        licenseList = callback.getLicenseList();

        System.out.println("Got " + licenseList.size() + " licenses from account!");

        downloadApp();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    private void downloadApp() {
        // Initiate the DepotDownloader, it is a Closable so it can be cleaned up when no longer used.
        // You will need to subscribe to LicenseListCallback to obtain your app licenses.
        try (var depotDownloader = new DepotDownloader(steamClient, licenseList, false)) {

            // Add this class as a listener of IDownloadListener
            depotDownloader.addListener(this);

            var pubItem = new PubFileItem(
                    /* (Required) appId */ 0,
                    /* (Required) pubFile */ 0,
                    /* (Optional) installToGameNameDirectory */ false,
                    /* (Optional) installDirectory */ null,
                    /* (Optional) verify */ false,
                    /* (Optional) downloadManifestOnly */ false
            );

            var ugcItem = new UgcItem(
                    /* (Required) appId */0,
                    /* (Required) ugcId */ 0,
                    /* (Optional) installToGameNameDirectory */ false,
                    /* (Optional) installDirectory */ null,
                    /* (Optional) verify */ false,
                    /* (Optional) downloadManifestOnly */ false
            );

            var appItem = new AppItem(
                    /* (Required) appId */ 1303350,
                    /* (Optional) installToGameNameDirectory */ true,
                    /* (Optional) installDirectory */ "steamapps",
                    /* (Optional) branch */ "public",
                    /* (Optional) branchPassword */ "",
                    /* (Optional) downloadAllPlatforms */ false,
                    /* (Optional) os */ "windows",
                    /* (Optional) downloadAllArchs */ false,
                    /* (Optional) osArch */ "64",
                    /* (Optional) downloadAllLanguages */ false,
                    /* (Optional) language */ "english",
                    /* (Optional) lowViolence */ false,
                    /* (Optional) depot */ List.of(),
                    /* (Optional) manifest */ List.of(),
                    /* (Optional) verify */ false,
                    /* (Optional) downloadManifestOnly */ false
            );

            // Items added are downloaded automatically in a FIFO (First-In, First-Out) queue.

            // Add a singular item to process.
            depotDownloader.add(appItem);

            // You can add a List of items to be processed.
            // depotDownloader.add(List.of(a, b, c));

            // Signal the downloader that no more items will be added.
            // Once all items in queue are done, 'completion' will signal that everything had finished.
            depotDownloader.finishAdding();

            // Block until we're done downloading.
            // Note: If you did not call `finishAdding()` before awaiting, depotDownloader will be expecting
            // more items to be added to queue. It may look like a hang. You could call `close()` to finish too.
            depotDownloader.awaitCompletion();

            // Kotlin users can use:
            // depotDownloader.getCompletion().await()

            // Remove this class as a listener of IDownloadListener
            depotDownloader.removeListener(this);
        } finally {
            System.out.println("Done Downloading");
            steamUser.logOff();
        }
    }

    // Depot Downloader Callbacks.

    @Override
    public void onItemAdded(@NotNull DownloadItem item) {
        System.out.println("Item " + item.getAppId() + " added to queue.");
    }

    @Override
    public void onDownloadStarted(@NotNull DownloadItem item) {
        System.out.println("Item " + item.getAppId() + " download started.");
    }

    @Override
    public void onDownloadCompleted(@NotNull DownloadItem item) {
        System.out.println("Item " + item.getAppId() + " download completed.");
    }

    @Override
    public void onDownloadFailed(@NotNull DownloadItem item, @NotNull Throwable error) {
        System.out.println("Item " + item.getAppId() + " failed to download");
        System.err.println(error.getMessage());
    }

    @Override
    public void onStatusUpdate(@NotNull String message) {
        System.out.println("Status: " + message);
    }

    @Override
    public void onFileCompleted(int depotId, @NotNull String fileName, float depotPercentComplete) {
        var complete = String.format("%.2f%%", depotPercentComplete * 100f);
        System.out.println("Depot " + depotId + " with file " + fileName + " completed. " + complete);
    }

    @Override
    public void onDepotCompleted(int depotId, long compressedBytes, long uncompressedBytes) {
        System.out.println("Depot " + depotId + " completed.");
        System.out.println("\t" + compressedBytes + " compressed bytes");
        System.out.println("\t" + uncompressedBytes + " uncompressed bytes");
    }
}
