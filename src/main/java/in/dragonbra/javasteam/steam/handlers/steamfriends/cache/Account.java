package in.dragonbra.javasteam.steam.handlers.steamfriends.cache;

import in.dragonbra.javasteam.types.SteamID;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public abstract class Account {
    private SteamID steamID = new SteamID();

    private String name;

    private byte[] avatarHash;

    public SteamID getSteamID() {
        return steamID;
    }

    public void setSteamID(SteamID steamID) {
        this.steamID = steamID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getAvatarHash() {
        return avatarHash;
    }

    public void setAvatarHash(byte[] avatarHash) {
        this.avatarHash = avatarHash;
    }
}
