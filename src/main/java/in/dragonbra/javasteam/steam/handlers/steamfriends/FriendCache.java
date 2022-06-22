package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EClanRelationship;
import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EPersonaStateFlag;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lossy
 * @since 2022-06-12
 */
public class FriendCache {

    abstract static class Account {

        private SteamID steamID;

        private String name;

        private byte[] avatarHash;

        public Account() {
            this.steamID = new SteamID();
        }

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

    static class User extends Account {

        private EFriendRelationship relationship;

        private EPersonaState personaState;

        private EnumSet<EPersonaStateFlag> personaStateFlags;

        private int gameAppID;

        private GameID gameID;

        private String gameName;

        public User() {
            this.gameID = new GameID();
            this.setPersonaState(EPersonaState.Offline); // JavaSteam edit: Not sure to keep this here.
        }

        public EFriendRelationship getRelationship() {
            return relationship;
        }

        public void setRelationship(EFriendRelationship relationship) {
            this.relationship = relationship;
        }

        public EPersonaState getPersonaState() {
            return personaState;
        }

        public void setPersonaState(EPersonaState personaState) {
            this.personaState = personaState;
        }

        public EnumSet<EPersonaStateFlag> getPersonaStateFlags() {
            return personaStateFlags;
        }

        public void setPersonaStateFlags(EnumSet<EPersonaStateFlag> personaStateFlags) {
            this.personaStateFlags = personaStateFlags;
        }

        public int getGameAppID() {
            return gameAppID;
        }

        public void setGameAppID(int gameAppID) {
            this.gameAppID = gameAppID;
        }

        public GameID getGameID() {
            return gameID;
        }

        public void setGameID(GameID gameID) {
            this.gameID = gameID;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }
    }

    static class Clan extends Account {

        public Clan() {
        }

        private EClanRelationship relationship;

        public EClanRelationship getRelationship() {
            return relationship;
        }

        public void setRelationship(EClanRelationship relationship) {
            this.relationship = relationship;
        }
    }

    static class AccountList<T extends Account> extends ConcurrentHashMap<SteamID, T> {

        public T getAccount(SteamID steamId, Class<? extends T> cls) {
            T account;

            try {
                account = cls.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            account.setSteamID(steamId);

            // Janky java 7 hack for computeIfAbsent.
            T v = this.get(steamId);
            if (v == null) {
                v = (v = this.putIfAbsent(steamId, account)) == null ? account : v;
            }
            return v;
        }
    }

    static class AccountCache {
        private final User localUser;

        private final AccountList<User> users;

        private final AccountList<Clan> clans;

        public AccountCache() {
            localUser = new User();

            users = new AccountList<>();
            clans = new AccountList<>();
        }

        public User getUser(SteamID steamID) {
            if (isLocalUser(steamID)) {
                return localUser;
            } else {
                return users.getAccount(steamID, User.class);
            }
        }

        public Clan getClan(SteamID steamID) {
            return clans.getAccount(steamID, Clan.class);
        }

        public User getLocalUser() {
            return localUser;
        }

        public AccountList<User> getUsers() {
            return users;
        }

        public AccountList<Clan> getClans() {
            return clans;
        }

        public boolean isLocalUser(SteamID steamID) {
            return localUser.getSteamID() == steamID;
        }
    }
}
