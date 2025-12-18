package in.dragonbra.javasteamsamples._030_counterstrikegc;

import in.dragonbra.javasteam.base.gc.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.protobufs.cs.Cstrike15Gcmessages;
import in.dragonbra.javasteam.protobufs.cs.GcsdkGcmessages;
import in.dragonbra.javasteam.protobufs.cs.Gcsystemmsgs;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver;
import in.dragonbra.javasteam.steam.authentication.AuthPollResult;
import in.dragonbra.javasteam.steam.authentication.AuthSessionDetails;
import in.dragonbra.javasteam.steam.authentication.AuthenticationException;
import in.dragonbra.javasteam.steam.authentication.UserConsoleAuthenticator;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.SteamGameCoordinator;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import static in.dragonbra.javasteam.protobufs.cs.Cstrike15Gcmessages.ECsgoGCMsg.k_EMsgGCCStrike15_v2_ClientRequestPlayersProfile;
import static in.dragonbra.javasteam.protobufs.cs.Cstrike15Gcmessages.ECsgoGCMsg.k_EMsgGCCStrike15_v2_PlayersProfile;
import static in.dragonbra.javasteam.protobufs.cs.Gcsystemmsgs.EGCBaseClientMsg.k_EMsgGCClientWelcome;

/**
 * @author Lossy 23-09-2025
 */
public class SampleCounterStrikeGC implements Runnable {

    private SteamClient steamClient;

    private SteamGameCoordinator steamGameCoordinator;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    public SampleCounterStrikeGC(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample 030: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleCounterStrikeGC(args[0], args[1]).run();
    }

    @Override
    public void run() {
        steamClient = new SteamClient();

        CallbackManager manager = new CallbackManager(steamClient);

        steamUser = steamClient.getHandler(SteamUser.class);

        steamGameCoordinator = steamClient.getHandler(SteamGameCoordinator.class);

        List<Closeable> subscriptions = new ArrayList<>();

        subscriptions.add(manager.subscribe(ConnectedCallback.class, this::onConnected));
        subscriptions.add(manager.subscribe(DisconnectedCallback.class, this::onDisconnected));
        subscriptions.add(manager.subscribe(LoggedOnCallback.class, this::onLoggedOn));
        subscriptions.add(manager.subscribe(LoggedOffCallback.class, this::onLoggedOff));
        subscriptions.add(manager.subscribe(MessageCallback.class, this::onGCMessage));

        isRunning = true;

        System.out.println("Connecting to steam...");

        steamClient.connect();

        while (isRunning) {
            manager.runWaitCallbacks(1000L);
        }

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

        var shouldRememberPassword = false;

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = user;
        authDetails.password = pass;
        authDetails.persistentSession = shouldRememberPassword;
        authDetails.authenticator = new UserConsoleAuthenticator();

        try {
            var authSession = steamClient.getAuthentication().beginAuthSessionViaCredentials(authDetails).get();

            AuthPollResult pollResponse = authSession.pollingWaitForResult().get();

            LogOnDetails details = new LogOnDetails();
            details.setUsername(pollResponse.getAccountName());
            details.setAccessToken(pollResponse.getRefreshToken());

            details.setLoginID(149);

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
        System.out.println("Disconnected from Steam. User initialized: " + callback.isUserInitiated());

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

        // Tell steam we want to play CS2.
        startPlayingGame(730);

        // Establish a connection to the CS2 Game Coordinator.
        sendHello();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }

    private void onGCMessage(MessageCallback messageCallback) {
        var messageType = messageCallback.getMessage().getMsgType();

        System.out.println("App ID: " + messageCallback.getAppID());
        System.out.println("EMsg: " + messageCallback.getEMsg());
        System.out.println("Message Type: " + messageType);

        if (messageType == k_EMsgGCClientWelcome.getNumber()) {
            // GC acknowledges us, we can now send further GC messages.
            System.out.println("GC has Welcomed us!");

            // Let's grab a random notable CS2 player for this example.
            var sid = new SteamID(76561198386265483L);

            // Sanity check
            if (!sid.isValid() ||
                    sid.getAccountUniverse() != EUniverse.Public ||
                    sid.getAccountType() != EAccountType.Individual ||
                    sid.getAccountInstance() != SteamID.DESKTOP_INSTANCE
            ) {
                System.err.println("Invalid SteamID");
                stopPlayingGame();
                return;
            }

            var profileRequest = new ClientGCMsgProtobuf<Cstrike15Gcmessages.CMsgGCCStrike15_v2_ClientRequestPlayersProfile.Builder>(
                    Cstrike15Gcmessages.CMsgGCCStrike15_v2_ClientRequestPlayersProfile.class,
                    k_EMsgGCCStrike15_v2_ClientRequestPlayersProfile.getNumber()
            );
            profileRequest.getBody().setAccountId((int) sid.getAccountID());
            profileRequest.getBody().setRequestLevel(32);

            steamGameCoordinator.send(profileRequest, 730);
        }

        if (messageType == k_EMsgGCCStrike15_v2_PlayersProfile.getNumber()) {
            // Display our results
            var response = new ClientGCMsgProtobuf<Cstrike15Gcmessages.CMsgGCCStrike15_v2_PlayersProfile.Builder>(
                    Cstrike15Gcmessages.CMsgGCCStrike15_v2_PlayersProfile.class,
                    messageCallback.getMessage()
            );

            for (var profile : response.getBody().getAccountProfilesList()) {
                System.out.println("Account ID: " + profile.getAccountId());
                System.out.println("Level: " + profile.getPlayerLevel());
            }

            System.out.println("Example done, logging off!");
            stopPlayingGame();
            steamUser.logOff();
        }
    }

    /**
     * Stop playing CS2
     */
    @SuppressWarnings("unused")
    private void stopPlayingGame() {
        startPlayingGame(0);
    }

    private void startPlayingGame(int appId) {
        var gamesPlayed = new ClientMsgProtobuf<SteammessagesClientserver.CMsgClientGamesPlayed.Builder>(
                SteammessagesClientserver.CMsgClientGamesPlayed.class,
                EMsg.ClientGamesPlayed
        );
        gamesPlayed.getBody().addGamesPlayedBuilder().setGameId(appId);

        steamClient.send(gamesPlayed);
    }

    private void sendHello() {
        var hello = new ClientGCMsgProtobuf<GcsdkGcmessages.CMsgClientHello.Builder>(
                GcsdkGcmessages.CMsgClientHello.class,
                Gcsystemmsgs.EGCBaseClientMsg.k_EMsgGCClientHello.getNumber()
        );
        hello.getBody().setVersion(2000244);

        steamGameCoordinator.send(hello, 730);
    }
}
