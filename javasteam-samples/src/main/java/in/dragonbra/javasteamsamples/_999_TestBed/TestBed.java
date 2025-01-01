package in.dragonbra.javasteamsamples._999_TestBed;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamapps.PICSRequest;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.util.NetHookNetworkListener;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is just a test bed to nethook various packets to later be added in tests
 * <p>
 * This program shouldn't be counted as a sample to 'test' for PRs.
 * <p>
 *
 * @author Lossy
 * @since 31/12/2024
 */
public class TestBed implements Runnable {

    private final SteamClient steamClient = new SteamClient();

    private final CallbackManager manager = new CallbackManager(steamClient);

    private final SteamUser steamUser = steamClient.getHandler(SteamUser.class);

    private final SteamApps steamApps = steamClient.getHandler(SteamApps.class);

    private final List<Closeable> callbackMsgs = new ArrayList<>();

    private boolean isRunning;


    public TestBed() {
    }

    public static void main(String[] args) {
        LogManager.addListener(new DefaultLogListener());

        new TestBed().run();
    }

    @Override
    public void run() {
        steamClient.setDebugNetworkListener(new NetHookNetworkListener());

        callbackMsgs.add(manager.subscribe(ConnectedCallback.class, this::onConnected));
        callbackMsgs.add(manager.subscribe(DisconnectedCallback.class, this::onDisconnected));

        callbackMsgs.add(manager.subscribe(LoggedOnCallback.class, this::onLoggedOn));
        callbackMsgs.add(manager.subscribe(LoggedOffCallback.class, this::onLoggedOff));

        isRunning = true;

        System.out.println("Connecting to steam...");

        steamClient.connect();

        while (isRunning) {
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to steam! Logging in...");

        steamUser.logOnAnonymous();
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam, reconnecting in 5...");

        isRunning = false;
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult());
            isRunning = false;
            return;
        }

        System.out.println("Successfully logged on!");

        steamApps.picsGetProductInfo(new PICSRequest(480), null);
        steamApps.picsGetProductInfo(new PICSRequest(480), null, true);

        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        for (var cb : callbackMsgs) {
            try {
                cb.close();
            } catch (Exception ignored) {
            }
        }
    }
}
