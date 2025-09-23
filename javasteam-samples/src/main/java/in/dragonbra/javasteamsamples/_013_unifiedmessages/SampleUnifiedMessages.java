package in.dragonbra.javasteamsamples._013_unifiedmessages;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUIMode;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChatRoom_IncomingChatMessage_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesGamenotificationsSteamclient.CGameNotifications_OnNotificationsRequested_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.service.ChatRoomClient;
import in.dragonbra.javasteam.rpc.service.FriendMessagesClient;
import in.dragonbra.javasteam.rpc.service.GameNotificationsClient;
import in.dragonbra.javasteam.rpc.service.Player;
import in.dragonbra.javasteam.steam.authentication.AuthPollResult;
import in.dragonbra.javasteam.steam.authentication.AuthSessionDetails;
import in.dragonbra.javasteam.steam.authentication.UserConsoleAuthenticator;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification;
import in.dragonbra.javasteam.steam.handlers.steamuser.ChatMode;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.util.concurrent.ExecutionException;

/**
 * @author Lossy
 * @since 2023-01-04
 * <p>
 * Sample 8: Unified Messages
 * <p>
 * this sample introduces the usage of the unified service API
 * <p>
 * unified services are a type of webapi service that can be accessed with either
 * HTTP requests or through the Steam network
 * <p>
 * in this case, this sample will demonstrate using the IPlayer unified service
 * through the connection to steam
 */
@SuppressWarnings({"resource", "FieldCanBeLocal"})
public class SampleUnifiedMessages implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private SteamUnifiedMessages steamUnifiedMessages;

    private Player playerService;

    private boolean isRunning;

    private final String user;

    private final String pass;

    JobID badgeRequest;

    JobID favoriteBadge;

    public SampleUnifiedMessages(String user, String pass) {
        this.user = user;
        this.pass = pass;

        this.badgeRequest = JobID.INVALID;
        this.favoriteBadge = JobID.INVALID;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample 013: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleUnifiedMessages(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);

        // get the steam unified messages handler, which is used for sending and receiving responses from the unified service api
        steamUnifiedMessages = steamClient.getHandler(SteamUnifiedMessages.class);

        // The SteamUnifiedMessages handler can be removed if it's not needed.
        // steamClient.removeHandler(SteamUnifiedMessages.class);

        // we also want to create our local service interface, which will help us build requests to the unified api
        playerService = steamUnifiedMessages.createService(Player.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        // subscribe to incoming messages from the GameNotificationsClient service
        manager.subscribeServiceNotification(
                GameNotificationsClient.class,
                CGameNotifications_OnNotificationsRequested_Notification.Builder.class,
                this::onGameStartedNotification
        );
        // subscribe to others
        manager.subscribeServiceNotification(
                ChatRoomClient.class,
                CChatRoom_IncomingChatMessage_Notification.Builder.class,
                this::onIncomingChatRoomMessage
        );
        manager.subscribeServiceNotification(
                FriendMessagesClient.class,
                CFriendMessages_IncomingMessage_Notification.Builder.class,
                this::onIncomingMessage
        );

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

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = user;
        authDetails.password = pass;
        authDetails.authenticator = new UserConsoleAuthenticator();

        try {
            var authSession = steamClient.getAuthentication().beginAuthSessionViaCredentials(authDetails).get();

            AuthPollResult pollResponse = authSession.pollingWaitForResult().get();

            LogOnDetails details = new LogOnDetails();
            details.setUsername(pollResponse.getAccountName());
            details.setAccessToken(pollResponse.getRefreshToken());
            details.setUiMode(EUIMode.Unknown);
            details.setChatMode(ChatMode.NEW_STEAM_CHAT);

            steamUser.logOn(details);
        } catch (Exception e) {
            System.err.println("An error occurred:" + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            steamUser.logOff();
        }
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

        // first, build our request object, these are autogenerated and can normally be found in the in.dragonbra.javasteam.protobufs.steamclient package
        CPlayer_GetGameBadgeLevels_Request req = CPlayer_GetGameBadgeLevels_Request.newBuilder()
                .setAppid(440) // we want to know our 440 (TF2) badge level
                .build();

        // now let's send the request and await for the response
        try {
            var response = playerService.getGameBadgeLevels(req).toFuture().get();

            System.out.println("Main Request:");
            if (response.getResult() != EResult.OK) {
                System.err.println("Unified service request failed with " + response.getResult());
            }
            System.out.println("Our player level is " + response.getBody().getPlayerLevel());
            for (var badge : response.getBody().getBadgesList()) {
                System.out.println("Badge series " + badge.toString() + " is level " + badge.getLevel());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // alternatively, the request can be made using SteamUnifiedMessages directly, but then you must build the service request name manually
        // the name format is in the form of <Service>.<Method>#<Version>
        try {
            var responseAlt = steamUnifiedMessages.sendMessage(
                    CPlayer_GetGameBadgeLevels_Response.Builder.class,
                    "Player.GetGameBadgeLevels#1",
                    req
            ).toFuture().get();

            System.out.println("Alt Request");
            if (responseAlt.getResult() != EResult.OK) {
                System.err.println("Unified service request failed with " + responseAlt.getResult());
            }
            System.out.println("Our player level is " + responseAlt.getBody().getPlayerLevel());
            for (var badge : responseAlt.getBody().getBadgesList()) {
                System.out.println("Badge series " + badge.toString() + " is level " + badge.getLevel());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // now that we've completed our task, lets log off after a few seconds to receive possible notifications
        // steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    // Below demonstrates some incoming notifications from Service Methods via Unified.

    private void onGameStartedNotification(
            ServiceMethodNotification<CGameNotifications_OnNotificationsRequested_Notification.Builder> notification
    ) {
        System.out.println("User with id " + notification.getBody().getSteamid() + " started the game: " + notification.getBody().getAppid());
    }

    private void onIncomingChatRoomMessage(
            ServiceMethodNotification<CChatRoom_IncomingChatMessage_Notification.Builder> notification
    ) {
        System.out.println("onIncomingChatMessage");
        System.out.println("Group ID: " + notification.getBody().clearChatGroupId());
        System.out.println("Chat Name: " + notification.getBody().getChatName());
        System.out.println("Sender: " + new SteamID(notification.getBody().getSteamidSender()).convertToUInt64());
        System.out.println("Message: " + notification.getBody().getMessage());
    }

    private void onIncomingMessage(
            ServiceMethodNotification<CFriendMessages_IncomingMessage_Notification.Builder> notification
    ) {
        System.out.println("onIncomingChatMessage");
        if (notification.getBody().getChatEntryType() == 2) {
            System.out.println("Friend is typing...");
        }
        if (notification.getBody().getChatEntryType() == 1) {
            System.out.println("Message: " + notification.getBody().getMessage());
        }
    }
}
