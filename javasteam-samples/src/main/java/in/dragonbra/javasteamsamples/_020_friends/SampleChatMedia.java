 package in.dragonbra.javasteamsamples._020_friends;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.AuthPollResult;
import in.dragonbra.javasteam.steam.authentication.AuthSessionDetails;
import in.dragonbra.javasteam.steam.authentication.AuthenticationException;
import in.dragonbra.javasteam.steam.authentication.CredentialsAuthSession;
import in.dragonbra.javasteam.steam.authentication.SteamAuthentication;
import in.dragonbra.javasteam.steam.authentication.UserConsoleAuthenticator;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.types.UGCHandle;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

//
// Sample 020.2: Chat media
//
// this sample logs on and sends an image or video to the chat with a friend.
//
// media doesn't go over the connection to Steam like a text message does. It is uploaded
// to the community website, and Steam then posts it to the chat for us. The website wants
// the access token of the logged-in user, so we log on with an auth session to get one.
//
// once it is sent, the sample asks Steam for the details of the upload and prints
// the address the file is stored at.
//
// usage: <username> <password> <friend steamid64> <path to a file> [spoiler]
// supported files are: png, jpg, gif, webm, mpg and ogv, up to 10 MiB.
// add "spoiler" to the end to hide the media in the chat until it is clicked.
//

/**
 * @author lossy
 * @since 2026-11-11
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleChatMedia implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private SteamCloud steamCloud;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private final SteamID friend;

    private final Path file;

    private final boolean spoiler;

    private String accessToken;

    public SampleChatMedia(String user, String pass, SteamID friend, Path file, boolean spoiler) {
        this.user = user;
        this.pass = pass;
        this.friend = friend;
        this.file = file;
        this.spoiler = spoiler;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Sample 020: Usage: <username> <password> <friend steamid64> <path to a file> [spoiler]");
            return;
        }

        SteamID friend;
        try {
            friend = new SteamID(Long.parseUnsignedLong(args[2]));
        } catch (NumberFormatException e) {
            System.out.println("Sample 020: The friend has to be a 64-bit SteamID, like 76561197960287930");
            return;
        }

        Path file = Path.of(args[3]);
        if (!Files.isRegularFile(file)) {
            System.out.println("Sample 020: No file found at " + file.toAbsolutePath());
            return;
        }

        // an optional fifth argument hides the media in the chat until it is clicked
        boolean spoiler = args.length > 4 && args[4].equalsIgnoreCase("spoiler");

        LogManager.addListener(new DefaultLogListener());

        new SampleChatMedia(args[0], args[1], friend, file, spoiler).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // get the steamcloud handler, which is used for uploading the media
        steamCloud = steamClient.getHandler(SteamCloud.class);

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
        SteamAuthentication auth = new SteamAuthentication(steamClient);

        try {
            CredentialsAuthSession authSession = auth.beginAuthSessionViaCredentials(authSessionDetails).get();

            AuthPollResult pollResponse = authSession.pollingWaitForResult().get();

            LogOnDetails logOnDetails = new LogOnDetails();
            logOnDetails.setUsername(pollResponse.getAccountName());
            logOnDetails.setAccessToken(pollResponse.getRefreshToken());

            // Set LoginID to a non-zero value if you have another client connected using the same account,
            // the same private ip, and same public ip.
            logOnDetails.setLoginID(149);

            steamUser.logOn(logOnDetails);

            // the access token is what signs us in to the community website, keep it for the upload.
            // it expires after a day, see sample 002 for how to renew it.
            accessToken = pollResponse.getAccessToken();
        } catch (Exception e) {
            System.err.println(e.getMessage());

            // List a couple of exceptions that could be important to handle.
            if (e instanceof AuthenticationException) {
                System.out.println("An Authentication error has occurred. " + e.getMessage());
            }

            if (e instanceof CancellationException) {
                System.out.println("An Cancellation exception was raised. Usually means a timeout occurred. " + e.getMessage());
            }

            isRunning = false;
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

        // at this point, we'd be able to perform actions on Steam

        try {
            UGCHandle ugcHandle = sendMedia();

            if (ugcHandle != null) {
                printDetails(ugcHandle);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // for this sample we'll just log off
        // steamUser.logOff();
    }

    /**
     * Uploads the file to the chat with the friend.
     *
     * @return the handle of the uploaded file, or null if it could not be sent.
     */
    private UGCHandle sendMedia() throws InterruptedException {
        byte[] media;
        try {
            media = Files.readAllBytes(file);
        } catch (IOException e) {
            System.err.println("Unable to read " + file + ": " + e.getMessage());
            return null;
        }

        System.out.println("Sending " + file.getFileName() + " (" + media.length + " bytes) to " + friend.convertToUInt64() + "...");

        try {
            // the type of the file, and the size of an image, are worked out from the file itself.
            // the upload takes a few requests, so we get a future back and wait for it here.
            // to send to a group chat instead, use uploadChatMediaToChatRoom with the IDs of the group and channel.
            long ugcId = steamCloud.uploadChatMediaToUser(
                    accessToken,
                    friend,
                    media,
                    file.getFileName().toString(),
                    spoiler
            ).get();

            // nothing else to do to get it delivered, Steam has posted the media to the chat
            System.out.println("Sent! Steam stored it with UGC ID " + Long.toUnsignedString(ugcId));

            return new UGCHandle(ugcId);
        } catch (ExecutionException e) {
            // an IllegalArgumentException means the file is too large or not a supported format,
            // an IOException that a request failed, the access token being rejected for one.
            System.err.println("Unable to send the media: " + e.getCause().getMessage());
            return null;
        }
    }

    /**
     * Asks Steam where the uploaded file ended up.
     */
    private void printDetails(UGCHandle ugcHandle) throws InterruptedException {
        try {
            // the upload only hands back an ID. The details of what is stored behind it, the address
            // it can be downloaded from among them, can be asked for over the connection to Steam.
            UGCDetailsCallback details = steamCloud.requestUGCDetails(ugcHandle).get();

            if (details.getResult() != EResult.OK) {
                System.out.println("Unable to get the details of the upload: " + details.getResult());
                return;
            }

            System.out.println("  URL:      " + details.getUrl());
            System.out.println("  Name:     " + details.getFileName());
            System.out.println("  Size:     " + details.getFileSize() + " bytes");
            System.out.println("  SHA-1:    " + details.getFileSHA());
            System.out.println("  Uploaded: " + details.getTimestamp());
        } catch (ExecutionException e) {
            // the media was still sent, we just couldn't look it up
            System.err.println("Unable to get the details of the upload: " + e.getCause().getMessage());
        }
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
