package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EPersonaStateFlag;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPersonaState;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetAddress;
import java.util.Date;
import java.util.EnumSet;

/**
 * Represents the persona state of a friend.
 */
public class PersonaState {

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

    public PersonaState(CMsgClientPersonaState.Friend friend) {
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
        clanTag = friend.getClanTag();

        onlineSessionInstances = friend.getOnlineSessionInstances();
    }

    /**
     * @return the status flags. This shows what has changed.
     */
    public EnumSet<EClientPersonaStateFlag> getStatusFlags() {
        return statusFlags;
    }

    /**
     * @return the friend's {@link SteamID}
     */
    public SteamID getFriendID() {
        return friendID;
    }

    /**
     * @return the state.
     */
    public EPersonaState getState() {
        return state;
    }

    /**
     * @return the state flags.
     */
    public EnumSet<EPersonaStateFlag> getStateFlags() {
        return stateFlags;
    }

    /**
     * @return the game app ID.
     */
    public int getGameAppID() {
        return gameAppID;
    }

    /**
     * @return the game ID.
     */
    public GameID getGameID() {
        return gameID;
    }

    /**
     * @return the name of the game.
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * @return the game server IP.
     */
    public InetAddress getGameServerIP() {
        return gameServerIP;
    }

    /**
     * @return the game server port.
     */
    public int getGameServerPort() {
        return gameServerPort;
    }

    /**
     * @return the query port.
     */
    public int getQueryPort() {
        return queryPort;
    }

    /**
     * @return the source {@link SteamID}.
     */
    public SteamID getSourceSteamID() {
        return sourceSteamID;
    }

    /**
     * @return the game data blob.
     */
    public byte[] getGameDataBlob() {
        return gameDataBlob;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the avatar hash.
     */
    public byte[] getAvatarHash() {
        return avatarHash;
    }

    /**
     * @return the last log off.
     */
    public Date getLastLogOff() {
        return lastLogOff;
    }

    /**
     * @return the last log on.
     */
    public Date getLastLogOn() {
        return lastLogOn;
    }

    /**
     * @return the clan rank.
     */
    public int getClanRank() {
        return clanRank;
    }

    /**
     * @return the clan tag.
     */
    public String getClanTag() {
        return clanTag;
    }

    /**
     * @return the online session instance.
     */
    public int getOnlineSessionInstances() {
        return onlineSessionInstances;
    }
}
