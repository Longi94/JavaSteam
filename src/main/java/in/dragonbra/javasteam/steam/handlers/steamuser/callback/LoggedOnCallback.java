package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import in.dragonbra.javasteam.enums.EAccountFlags;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientLogOnResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.ParentalSettings;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetAddress;
import java.util.Date;
import java.util.EnumSet;

/**
 * This callback is returned in response to an attempt to log on to the Steam3 network through {@link SteamUser}.
 */
public class LoggedOnCallback extends CallbackMsg {

    private EResult result;

    private EResult extendedResult;

    private int outOfGameSecsPerHeartbeat;

    private int inGameSecsPerHeartbeat;

    private InetAddress publicIP;

    private Date serverTime;

    private EnumSet<EAccountFlags> accountFlags;

    private SteamID clientSteamID;

    private String emailDomain;

    private int cellID;

    private int cellIDPingThreshold;

    private byte[] steam2Ticket;

    private String webAPIUserNonce;

    private String ipCountryCode;

    private String vanityURL;

    private int numLoginFailuresToMigrate;

    private int numDisconnectsToMigrate;

    private ParentalSettings parentalSettings;

    public LoggedOnCallback(CMsgClientLogonResponse.Builder resp) {
        result = EResult.from(resp.getEresult());
        extendedResult = EResult.from(resp.getEresultExtended());

        outOfGameSecsPerHeartbeat = resp.getLegacyOutOfGameHeartbeatSeconds();
        inGameSecsPerHeartbeat = resp.getHeartbeatSeconds();

        publicIP = NetHelpers.getIPAddress(resp.getPublicIp().getV4()); // Has ipV6 support, but still using ipV4
        serverTime = new Date(resp.getRtime32ServerTime() * 1000L);

        accountFlags = EAccountFlags.from(resp.getAccountFlags());

        clientSteamID = new SteamID(resp.getClientSuppliedSteamid());

        emailDomain = resp.getEmailDomain();

        cellID = resp.getCellId();
        cellIDPingThreshold = resp.getCellIdPingThreshold();

        steam2Ticket = resp.getSteam2Ticket().toByteArray();

        ipCountryCode = resp.getIpCountryCode();

        webAPIUserNonce = resp.getWebapiAuthenticateUserNonce();

        vanityURL = resp.getVanityUrl();

        numLoginFailuresToMigrate = resp.getCountLoginfailuresToMigrate();
        numDisconnectsToMigrate = resp.getCountDisconnectsToMigrate();

        ByteString data = resp.getParentalSettings();
        if (data != null) {
            try {
                parentalSettings = ParentalSettings.parseFrom(data);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    public LoggedOnCallback(MsgClientLogOnResponse resp) {
        result = resp.getResult();

        outOfGameSecsPerHeartbeat = resp.getOutOfGameHeartbeatRateSec();
        inGameSecsPerHeartbeat = resp.getInGameHeartbeatRateSec();

        publicIP = NetHelpers.getIPAddress((int) resp.getIpPublic());

        serverTime = new Date(resp.getServerRealTime() * 1000L);

        clientSteamID = resp.getClientSuppliedSteamId();
    }

    public LoggedOnCallback(EResult result) {
        this.result = result;
    }

    /**
     * @return the result of the logon as {@link EResult}.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the extended result of the logon as {@link EResult}.
     */
    public EResult getExtendedResult() {
        return extendedResult;
    }

    /**
     * @return the out of game secs per heartbeat value.
     * This is used internally to initialize heartbeating.
     */
    public int getOutOfGameSecsPerHeartbeat() {
        return outOfGameSecsPerHeartbeat;
    }

    /**
     * @return the in game secs per heartbeat value.
     * This is used internally to initialize heartbeating.
     */
    public int getInGameSecsPerHeartbeat() {
        return inGameSecsPerHeartbeat;
    }

    /**
     * @return the public IP of the client.
     */
    public InetAddress getPublicIP() {
        return publicIP;
    }

    /**
     * @return the Steam3 server time.
     */
    public Date getServerTime() {
        return serverTime;
    }

    /**
     * @return the account flags assigned by the server. See {@link EAccountFlags}.
     */
    public EnumSet<EAccountFlags> getAccountFlags() {
        return accountFlags;
    }

    /**
     * @return the client steam ID as {@link SteamID}
     */
    public SteamID getClientSteamID() {
        return clientSteamID;
    }

    /**
     * @return the email domain.
     */
    public String getEmailDomain() {
        return emailDomain;
    }

    /**
     * @return the Steam2 CellID.
     */
    public int getCellID() {
        return cellID;
    }

    /**
     * @return the Steam2 CellID ping threshold.
     */
    public int getCellIDPingThreshold() {
        return cellIDPingThreshold;
    }

    /**
     * @return the Steam2 ticket.
     * This is used for authenticated content downloads in Steam2.
     * This field will only be set when {@link LogOnDetails#isRequestSteam2Ticket} has been set to true.
     */
    public byte[] getSteam2Ticket() {
        return steam2Ticket;
    }

    /**
     * @return the WebAPI authentication user nonce.
     */
    public String getWebAPIUserNonce() {
        return webAPIUserNonce;
    }

    /**
     * @return the IP country code.
     */
    public String getIpCountryCode() {
        return ipCountryCode;
    }

    /**
     * @return the vanity URL.
     */
    public String getVanityURL() {
        return vanityURL;
    }

    /**
     * @return the threshold for login failures before Steam wants the client to migrate to a new CM.
     */
    public int getNumLoginFailuresToMigrate() {
        return numLoginFailuresToMigrate;
    }

    /**
     * @return the threshold for disconnects before Steam wants the client to migrate to a new CM.
     */
    public int getNumDisconnectsToMigrate() {
        return numDisconnectsToMigrate;
    }

    /**
     * @return the Steam parental settings.
     */
    public ParentalSettings getParentalSettings() {
        return parentalSettings;
    }
}
