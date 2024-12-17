package in.dragonbra.javasteamsamples._024_downloaduserfiles;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamcloud.AppFileChangeList;
import in.dragonbra.javasteam.steam.handlers.steamcloud.AppFileInfo;
import in.dragonbra.javasteam.steam.handlers.steamcloud.FileDownloadInfo;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
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
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Oxters
 * @since 2024-12-17
 * <p>
 * Sample 24: Download User Files
 * <p>
 * this sample introduces the usage of the steam cloud API
 * <p>
 * steam cloud lets you download user files of apps from Steam
 * <p>
 * in this case, this sample will demonstrate how to download the user files
 * of a game called The Messenger
 */
@SuppressWarnings({"FieldCanBeLocal", "DuplicatedCode"})
public class SampleDownloadUserFiles implements Runnable {

    private final int THE_MESSENGER_APP_ID = 764790;

    private final String[] PATH_TYPES = new String[] {
        "%GameInstall%",
        "%WinMyDocuments%",
        "%WinAppDataLocal%",
        "%WinAppDataLocalLow%",
        "%WinAppDataRoaming%",
        "%WinSavedGames%",
        "%LinuxHome%",
        "%LinuxXdgDataHome%",
        "%LinuxXdgConfigHome%",
        "%MacHome%",
        "%MacAppSupport%",
    };

    private SteamClient steamClient;

    private CallbackManager manager;
    private SteamUser steamUser;
    private SteamCloud steamCloud;

    private boolean isRunning;

    private final String user;

    private final String pass;

    private final String twoFactor;

    public SampleDownloadUserFiles(String user, String pass, String twoFactor) {
        this.user = user;
        this.pass = pass;
        this.twoFactor = twoFactor;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Sample24: No username and password specified!");
            return;
        }

        LogManager.addListener(new DefaultLogListener());

        String twoFactor = null;
        if (args.length == 3)
            twoFactor = args[2];
        new SampleDownloadUserFiles(args[0], args[1], twoFactor).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClient();

        // create the callback manager which will route callbacks to function calls
        manager = new CallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser.class);
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

        // now that we are logged in, we can download the user files of an app
        downloadUserFiles();
        steamClient.disconnect();
    }

    private void downloadUserFiles() {
        try {
            AppFileChangeList appFileListChange = steamCloud.getAppFileListChange(THE_MESSENGER_APP_ID).get();
            List<String> pathPrefixes = appFileListChange.getPathPrefixes();
            for (AppFileInfo file : appFileListChange.getFiles()) {
                Path prefixPath;
                Path filePath;
                if (file.getPathPrefixIndex() < pathPrefixes.size()) {
                    prefixPath = Paths.get(pathPrefixes.get(file.getPathPrefixIndex()), file.getFilename());
                } else {
                    prefixPath = Paths.get("%GameInstall%", file.getFilename());
                }
                String tempPath = prefixPath.toString();
                for (String pathType : PATH_TYPES) {
                    //noinspection SpellCheckingInspection
                    tempPath = tempPath.replace(pathType, "userfiles/");
                }
                filePath = Paths.get(tempPath);

                FileDownloadInfo fileDownloadInfo = steamCloud.clientFileDownload(THE_MESSENGER_APP_ID, prefixPath.toString()).get();
                if (!fileDownloadInfo.getUrlHost().isEmpty()) {
                    String scheme;
                    if (fileDownloadInfo.getUseHttps()) {
                        scheme = "https://";
                    } else {
                        //noinspection HttpUrlsUsage
                        scheme = "http://";
                    }
                    String httpUrl = scheme + fileDownloadInfo.getUrlHost() + fileDownloadInfo.getUrlPath();
                    System.out.println("Downloading " + httpUrl);
                    String[] headers = fileDownloadInfo.getRequestHeaders().stream().map(header ->
                            new String[] { header.getName(), header.getValue() }
                    ).flatMap(Arrays::stream).toArray(String[]::new);

                    Request request = new Request.Builder()
                            .url(httpUrl)
                            .headers(new Headers(headers))
                            .build();
                    OkHttpClient httpClient = steamClient.getConfiguration().getHttpClient();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            System.out.println("File download of " + prefixPath + " was unsuccessful");
                            return;
                        }

                        try (InputStream byteStream = response.body().byteStream()) {
                            long bytesRead;
                            if (fileDownloadInfo.getFileSize() != fileDownloadInfo.getRawFileSize()) {
                                try (ZipInputStream zipStream = new ZipInputStream(byteStream)) {
                                    ZipEntry entry = zipStream.getNextEntry();
                                    if (entry == null) {
                                        System.out.println("Downloaded user file " + prefixPath + " has no zip entries");
                                        return;
                                    }
                                    bytesRead = copyToFile(filePath, zipStream);
                                    if (zipStream.getNextEntry() != null) {
                                        System.out.println("Downloaded user file " + prefixPath + " has more than one zip entry");
                                    }
                                }
                            } else {
                                bytesRead = copyToFile(filePath, byteStream);
                            }
                            if (bytesRead != fileDownloadInfo.getRawFileSize()) {
                                System.out.println("Bytes read from stream of " + prefixPath + " does not match expected size");
                            }
                        }
                    }
                } else {
                    System.out.println("URL host of " + prefixPath + " was empty");
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to download user files: " + e);
        }
    }
    private long copyToFile(Path filePath, InputStream inputStream) {
        long bytesRead = 0;
        try {
            Files.createDirectories(filePath.getParent());
            try (FileOutputStream fs = new FileOutputStream(filePath.toString())) {
                bytesRead = inputStream.transferTo(fs);
            }
        } catch (Exception e) {
            System.out.println("Failed to copy data to file: " + e);
        }
        return bytesRead;
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());

        isRunning = false;
    }
}

