package in.dragonbra.javasteamsamples;

import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamfriends.Friend;
import in.dragonbra.javasteam.steam.handlers.steamfriends.PersonaState;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendAddedCallback;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendsListCallback;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.PersonaStatesCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.AccountInfoCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;

/**
 * @author lngtr
 * @since 2018-02-28
 */
@SuppressWarnings("Duplicates")
public class SampleFriends implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private SteamFriends steamFriends;

    private boolean isRunning;

    private String user;

    private String pass;

    public SampleFriends(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample1: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleFriends(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);
        steamFriends = steamClient.getHandler(SteamFriends.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(AccountInfoCallback.class, this::onAccountInfo);
        manager.subscribe(FriendsListCallback.class, this::onFriendList);
        manager.subscribe(PersonaStatesCallback.class, this::onPersonaStates);
        manager.subscribe(FriendAddedCallback.class, this::onFriendAdded);

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

        // for this sample we wait for other callbacks to perform logic
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());
        isRunning = false;
    }

    private void onAccountInfo(AccountInfoCallback callback) {
        // before being able to interact with friends, you must wait for the account info callback
        // this callback is posted shortly after a successful logon

        // at this point, we can go online on friends, so lets do that
        steamFriends.setPersonaState(EPersonaState.Online);
    }

    private void onFriendList(FriendsListCallback callback) {
        // at this point, the client has received it's friends list

        int friendCount = callback.getFriendList().size();

        System.out.println("We have " + friendCount + " friends");

        for (int i = 0; i < friendCount; i++) {
            // steamids identify objects that exist on the steam network, such as friends, as an example
            SteamID steamIdFriend = callback.getFriendList().get(i).getSteamID();

            // we'll just display the STEAM_ rendered version
            System.out.println("Friend: " + steamIdFriend.render());
        }

        // we can also iterate over our friendslist to accept or decline any pending invites
        for (Friend friend : callback.getFriendList()) {
            if (friend.getRelationship() == EFriendRelationship.RequestRecipient) {
                // this user has added us, let's add him back
                steamFriends.addFriend(friend.getSteamID());
            }
        }
    }

    private void onFriendAdded(FriendAddedCallback callback) {
        // someone accepted our friend request, or we accepted one
        System.out.println(callback.getPersonaName() + " is now a friend");
    }

    private void onPersonaStates(PersonaStatesCallback callback) {
        // this callback is received when the persona state (friend information) of a friend changes

        // for this sample we'll simply display the names of the friends
        for (PersonaState state : callback.getPersonaStates()) {
            System.out.println("State change: " + state.getName() + " " + state.getState());
        }
    }
}
