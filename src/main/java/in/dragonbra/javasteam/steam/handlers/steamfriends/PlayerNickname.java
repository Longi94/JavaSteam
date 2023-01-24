package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPlayerNicknameList;
import in.dragonbra.javasteam.types.SteamID;

/**
 * Represents a nickname of a friend
 */
public class PlayerNickname {

    private final SteamID steamID;

    private final String nickname;

    public PlayerNickname(CMsgClientPlayerNicknameList.PlayerNickname nickname) {
        this.nickname = nickname.getNickname();
        steamID = new SteamID(nickname.getSteamid());
    }

    /**
     * @return the steam id of the friend
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return the nickname of the friend
     */
    public String getNickname() {
        return nickname;
    }
}
