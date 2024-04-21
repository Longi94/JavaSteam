package in.dragonbra.javasteamsamples._8UnifiedMessages;


import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUIMode;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.service.Player;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

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
            System.out.println("Sample8: No username and password specified!");
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

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(ServiceMethodResponse.class, this::onMethodResponse);
        manager.subscribe(ServiceMethodNotification.class, this::onMethodNotification);

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

        // Set our chat mode in order to use unified chat features
        ClientMsgProtobuf<CMsgClientUIMode.Builder> uiMode = new ClientMsgProtobuf<>(CMsgClientUIMode.class, EMsg.ClientCurrentUIMode);
        uiMode.getBody().setUimode(0);
        uiMode.getBody().setChatMode(2);

        // Send our ClientCurrentUIMode request
        steamClient.send(uiMode);

        // first, build our request object, these are autogenerated and can normally be found in the in.dragonbra.javasteam.protobufs.steamclient package
        CPlayer_GetFavoriteBadge_Request.Builder favoriteBadgeRequest = CPlayer_GetFavoriteBadge_Request.newBuilder();
        favoriteBadgeRequest.setSteamid(steamClient.getSteamID().convertToUInt64());

        // now let's send the request, this is done by building a class based off the IPlayer interface.
        Player playerService = new Player(steamUnifiedMessages);
        favoriteBadge = playerService.getFavoriteBadge(favoriteBadgeRequest.build()).getJobID();

        // second, build our request object, these are autogenerated and can normally be found in the in.dragonbra.javasteam.protobufs.steamclient package
        CPlayer_GetGameBadgeLevels_Request.Builder badgeLevelsRequest = CPlayer_GetGameBadgeLevels_Request.newBuilder();
        badgeLevelsRequest.setAppid(440);

        // alternatively, the request can be made using SteamUnifiedMessages directly, but then you must build the service request name manually
        // the name format is in the form of <Service>.<Method>#<Version>
        badgeRequest = steamUnifiedMessages.sendMessage("Player.GetGameBadgeLevels#1", badgeLevelsRequest.build()).getJobID();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    private void onMethodResponse(ServiceMethodResponse callback) {
        System.out.println("ServiceMethodResponse result: " + callback.getResult());

        // and check for success
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unified service request failed with " + callback.getResult());
            return;
        }

        // retrieve the deserialized response for the request we made
        // notice the naming pattern
        // for requests: CMyService_Method_Request
        // for responses: CMyService_Method_Response

        if (callback.getJobID().equals(badgeRequest)) {
            CPlayer_GetGameBadgeLevels_Response.Builder response = callback.getDeserializedResponse(CPlayer_GetGameBadgeLevels_Response.class);

            System.out.println("Our player level is " + response.getPlayerLevel());

            // If we have a list of badges, we'll print them out by series and level.
            response.getBadgesList().forEach(x ->
                    System.out.println("Badge series " + x.getSeries() + " is level " + x.getLevel())
            );

            badgeRequest = JobID.INVALID;
        }

        if (callback.getJobID().equals(favoriteBadge)) {
            CPlayer_GetFavoriteBadge_Response.Builder response = callback.getDeserializedResponse(CPlayer_GetFavoriteBadge_Response.class);

            System.out.println(
                    "Has favorite badge: " + response.hasHasFavoriteBadge() +
                            "\nBadge ID: " + response.getBadgeid() +
                            "\nCommunity item ID: " + response.getCommunityitemid() +
                            "\nItem Type: " + response.getItemType() +
                            "\nBorder Color: " + response.getBorderColor() +
                            "\nApp ID: " + response.getAppid() +
                            "\nLevel: " + response.getLevel()
            );

            favoriteBadge = JobID.INVALID;
        }
    }

    // This demonstrates some incoming notifications from Service Methods via Unified.
    void onMethodNotification(ServiceMethodNotification callback) {
        Object cbObject = callback.getBody();

        // There's an incoming message coming.
        if (cbObject instanceof CFriendMessages_IncomingMessage_Notification) {
            CFriendMessages_IncomingMessage_Notification message = (CFriendMessages_IncomingMessage_Notification) cbObject;

            if (message.getChatEntryType() == 2)
                System.out.println("Friend is typing...");

            if (message.getChatEntryType() == 1)
                System.out.println("Message: " + message.getMessage());
        }

        // There's a player preference change
        if (cbObject instanceof CPlayer_PerFriendPreferencesChanged_Notification) {
            CPlayer_PerFriendPreferencesChanged_Notification message = (CPlayer_PerFriendPreferencesChanged_Notification) cbObject;
            System.out.println("SteamID: " + message.getAccountid());
            System.out.println("NickName: " + message.getPreferences().getNickname());
        }
    }
}
