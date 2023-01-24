package in.dragonbra.javasteamsamples._7serverlist;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.discovery.FileServerListProvider;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

//
// Sample 7: ServerList
//
// this sample will give an example of how the server list can be used to
// optimize your chance of a successful connection.

/**
 * @author lngtr
 * @since 2021-10-11
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleServerList implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private int cellID = 0;

    public SampleServerList(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample7: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleServerList(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // if we've previously connected and saved our cellid, load it.
        File loginKeyFile = new File("cellid.txt");
        if (loginKeyFile.exists()) {
            try (Scanner s = new Scanner(loginKeyFile)) {
                cellID = Integer.parseInt(s.nextLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Error parsing cellid from cellid.txt. Continuing with cellid 0.");
            }
            System.out.println("Using persisted cell ID: " + cellID);
        } else {
            System.out.println("cellid file doesn't exist.");
        }

        SteamConfiguration configuration = SteamConfiguration.create(iSteamConfigurationBuilder -> {
            iSteamConfigurationBuilder.withCellID(cellID);
            iSteamConfigurationBuilder.withServerListProvider(new FileServerListProvider(new File("server_list.bin")));
        });

        // create our steamclient instance
        steamClient = new SteamClient(configuration);

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
                // if we recieve AccountLogonDenied or one of it's flavors (AccountLogonDeniedNoMailSent, etc)
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

        // save the current cellid somewhere. if we lose our saved server list, we can use this when retrieving
        // servers from the Steam Directory.
        try (FileWriter fw = new FileWriter("cellid.txt")) {
            fw.write(String.valueOf(callback.getCellID()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam

        System.out.println("Quit to log off.");
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}

