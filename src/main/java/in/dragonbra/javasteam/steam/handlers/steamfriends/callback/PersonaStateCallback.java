package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EPersonaStateFlag;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPersonaState;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetAddress;
import java.util.Date;
import java.util.EnumSet;

/**
 * This callback is fired in response to someone changing their friend details over the network.
 */
public class PersonaStateCallback extends CallbackMsg {
    private EnumSet<EClientPersonaStateFlag> statusFlags;

    private SteamID friendID;

    private EPersonaState state;

    private EnumSet<EPersonaStateFlag> stateFlags;

    private int gameAppID;

    private GameID gameID;

    private String gameName;

    private InetAddress gameServerIP;

    private int gameServerPort;

    private int queryPort;

    private SteamID sourceSteamID;

    private byte[] gameDataBlob;

    private String name;

    private byte[] avatarHash;

    private Date lastLogOff;

    private Date lastLogOn;

    private int clanRank;

    private String clanTag;

    private int onlineSessionInstances;

    private int publishedSessionID;

    public PersonaStateCallback(CMsgClientPersonaState.Friend friend) {
        statusFlags = EClientPersonaStateFlag.from(friend.getPersonaStateFlags());

        friendID = new SteamID(friend.getFriendid());
        state = EPersonaState.from(friend.getPersonaState());
        stateFlags = EPersonaStateFlag.from(friend.getPersonaStateFlags());

        gameAppID = friend.getGamePlayedAppId();
        gameID = new GameID(friend.getGameid());
        gameName = friend.getGameName();

        gameServerIP = NetHelpers.getIPAddress(friend.getGameServerIp());
        gameServerPort = friend.getGameServerPort();
        queryPort = friend.getQueryPort();

        sourceSteamID = new SteamID(friend.getSteamidSource());

        gameDataBlob = friend.getGameDataBlob().toByteArray();

        name = friend.getPlayerName();

        avatarHash = friend.getAvatarHash().toByteArray();

        lastLogOff = new Date(friend.getLastLogoff() * 1000L);
        lastLogOn = new Date(friend.getLastLogon() * 1000L);

        clanRank = friend.getClanRank();
        clanTag=  friend.getClanTag();

        onlineSessionInstances = friend.getOnlineSessionInstances();
        publishedSessionID = friend.getPublishedInstanceId();
    }

    public EnumSet<EClientPersonaStateFlag> getStatusFlags() {
        return statusFlags;
    }

    public SteamID getFriendID() {
        return friendID;
    }

    public EPersonaState getState() {
        return state;
    }

    public EnumSet<EPersonaStateFlag> getStateFlags() {
        return stateFlags;
    }

    public int getGameAppID() {
        return gameAppID;
    }

    public GameID getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public InetAddress getGameServerIP() {
        return gameServerIP;
    }

    public int getGameServerPort() {
        return gameServerPort;
    }

    public int getQueryPort() {
        return queryPort;
    }

    public SteamID getSourceSteamID() {
        return sourceSteamID;
    }

    public byte[] getGameDataBlob() {
        return gameDataBlob;
    }

    public String getName() {
        return name;
    }

    public byte[] getAvatarHash() {
        return avatarHash;
    }

    public Date getLastLogOff() {
        return lastLogOff;
    }

    public Date getLastLogOn() {
        return lastLogOn;
    }

    public int getClanRank() {
        return clanRank;
    }

    public String getClanTag() {
        return clanTag;
    }

    public int getOnlineSessionInstances() {
        return onlineSessionInstances;
    }

    public int getPublishedSessionID() {
        return publishedSessionID;
    }
}
