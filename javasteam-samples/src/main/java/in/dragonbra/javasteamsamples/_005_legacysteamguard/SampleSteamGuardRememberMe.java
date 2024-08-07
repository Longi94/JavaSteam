package in.dragonbra.javasteamsamples._005_legacysteamguard;

import in.dragonbra.javasteam.enums.EResult;
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
import in.dragonbra.javasteamsamples._000_authentication.SampleLogonAuthentication;
import in.dragonbra.javasteamsamples._001_authenticationwithqrcode.SampleLogonQRAuthentication;

import java.util.Scanner;

//
// Sample 5: SteamGuard
//
// this sample goes into detail for how to handle steamguard protected accounts and how to login to them
//
// SteamGuard works by enforcing a two factor authentication scheme
// upon first logon to an account with SG enabled, the steam server will email an authcode to the validated address of the account
// this authcode token can be used as the second factor during logon, but the token has a limited time span in which it is valid
//
// after a client logs on using the authcode, the steam server will generate a blob of random data that the client stores called a "sentry file"
// this sentry file is then used in all subsequent logons as the second factor
// ownership of this file provides proof that the machine being used to logon is owned by the client in question
//
// the usual login flow is thus:
// 1. connect to the server
// 2. logon to account with only username and password
// at this point, if the account is steamguard protected, the LoggedOnCallback will have a result of AccountLogonDenied
// the server will disconnect the client and email the authcode
//
// the login flow must then be restarted:
// 1. connect to server
// 2. logon to account using username, password, and authcode
// at this point, login wil succeed and a UpdateMachineAuthCallback callback will be posted with the sentry file data from the steam server
// the client will save the file, and reply to the server informing that it has accepted the sentry file
//
// all subsequent logons will use this flow:
// 1. connect to server
// 2. logon to account using username, password, and sha-1 hash of the sentry file

/**
 * WARNING!
 * This the old login flow, which may still work, but you will not receive machine auth.
 * This sample will be removed in the future.
 * <p>
 * See:
 * {@link SampleLogonAuthentication }
 * and
 * {@link SampleLogonQRAuthentication}
 *
 * @author lngtr
 * @see <a href="https://github.com/SteamRE/SteamKit/pull/1270#issuecomment-1768359942">SteamKit issue</a>
 * @since 2018-02-28
 */
@SuppressWarnings("FieldCanBeLocal")
public class SampleSteamGuardRememberMe implements Runnable {

    private SteamClient steamClient;

    private CallbackManager manager;

    private SteamUser steamUser;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private String authCode;

    private String twoFactorAuth;

    public SampleSteamGuardRememberMe(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample1: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        new SampleSteamGuardRememberMe(args[0], args[1]).run();
    }

    @Override
    public void run() {

        // create our steamclient instance
        steamClient = new SteamClient();

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

        // byte[] sentryHash = null;
        // File sentry = new File("sentry.bin");
        // if (sentry.exists()) {
        //     try {
        //         byte[] fileBytes = Files.readAllBytes(sentry.toPath());
        //         sentryHash = CryptoHelper.shaHash(fileBytes);
        //     } catch (IOException | NoSuchAlgorithmException e) {
        //         throw new RuntimeException(e);
        //     }
        // }

        LogOnDetails details = new LogOnDetails();
        details.setUsername(user);
        details.setPassword(pass);

        // in this sample, we pass in an additional authcode
        // this value will be null (which is the default) for our first logon attempt
        details.setAuthCode(authCode);

        // if the account is using 2-factor auth, we'll provide the two-factor code instead
        // this will also be null on our first logon attempt
        details.setTwoFactorCode(twoFactorAuth);

        // Set LoginID to a non-zero value if you have another client connected using the same account,
        // the same private ip, and same public ip.
        details.setLoginID(149);

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam, reconnecting in 5...");

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException ignored) {
            // Sleep Interrupted
        }

        steamClient.connect();
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        boolean isSteamGuard = callback.getResult() == EResult.AccountLogonDenied;
        boolean is2Fa = callback.getResult() == EResult.AccountLoginDeniedNeedTwoFactor;

        if (isSteamGuard || is2Fa) {
            System.out.println("This account is SteamGuard protected.");

            Scanner s = new Scanner(System.in);
            if (is2Fa) {
                System.out.print("Please enter your 2 factor auth code from your authenticator app: ");
                twoFactorAuth = s.nextLine();
            } else {
                System.out.print("Please enter the auth code sent to the email at " + callback.getEmailDomain());
                authCode = s.nextLine();
            }

            steamClient.disconnect();

            return;
        }

        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());

            isRunning = false;
            steamClient.disconnect();

            return;
        }

        System.out.println("Successfully logged on!");

        // at this point, we'd be able to perform actions on Steam
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}
