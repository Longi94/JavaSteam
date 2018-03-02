package in.dragonbra.javasteam.steam.handlers.steamfriends.cache;

import in.dragonbra.javasteam.types.SteamID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class AccountCache {
    private User localUser = new User();

    private Map<SteamID, User> users = Collections.synchronizedMap(new HashMap<SteamID, User>());

    private Map<SteamID, Clan> clans = Collections.synchronizedMap(new HashMap<SteamID, Clan>());

    public User getUser(SteamID steamID) {
        if (isLocalUser(steamID)) {
            return localUser;
        } else {
            if (users.containsKey(steamID)) {
                return users.get(steamID);
            } else {
                User user = new User();
                user.setSteamID(steamID);
                users.put(steamID, user);
                return user;
            }
        }
    }

    public boolean isLocalUser(SteamID steamID) {
        return localUser.getSteamID().equals(steamID);
    }

    public User getLocalUser() {
        return localUser;
    }

    public void setLocalUser(User localUser) {
        this.localUser = localUser;
    }

    public Map<SteamID, User> getUsers() {
        return users;
    }

    public void setUsers(Map<SteamID, User> users) {
        this.users = users;
    }

    public Map<SteamID, Clan> getClans() {
        return clans;
    }

    public void setClans(Map<SteamID, Clan> clans) {
        this.clans = clans;
    }

    public Clan getClan(SteamID steamID) {
        Clan clan = new Clan();
        clan.setSteamID(steamID);
        clans.put(steamID, clan);
        return clan;
    }
}
