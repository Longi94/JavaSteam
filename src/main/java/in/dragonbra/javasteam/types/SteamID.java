package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.util.CollectionUtils;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.compat.ObjectsCompat;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This 64-bit structure is used for identifying various objects on the Steam network.
 */
@SuppressWarnings("unused")
public class SteamID {

    private final BitVector64 steamID;

    private static final Pattern STEAM2_REGEX = Pattern.compile("STEAM_([0-4]):([0-1]):(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STEAM3_REGEX = Pattern.compile("\\[([AGMPCgcLTIUai]):([0-4]):(\\d+)(:(\\d+))?]");
    private static final Pattern STEAM3_FALLBACK_REGEX = Pattern.compile("\\[([AGMPCgcLTIUai]):([0-4]):(\\d+)(\\((\\d+)\\))?]");

    private static final Map<EAccountType, Character> ACCOUNT_TYPE_CHARS;

    static {

        ACCOUNT_TYPE_CHARS = Map.of(
                EAccountType.AnonGameServer, 'A',
                EAccountType.GameServer, 'G',
                EAccountType.Multiseat, 'M',
                EAccountType.Pending, 'P',
                EAccountType.ContentServer, 'C',
                EAccountType.Clan, 'g',
                EAccountType.Chat, 'T', // Lobby chat is 'L', Clan chat is 'c'
                EAccountType.Invalid, 'I',
                EAccountType.Individual, 'U',
                EAccountType.AnonUser, 'a'
        );
    }

    public static final char UNKNOWN_ACCOUNT_TYPE_CHAR = 'i';

    /**
     * The account instance value when representing all instanced {@link SteamID SteamIDs}.
     */
    public static final long ALL_INSTANCES = 0L;

    /**
     * The account instance value for a desktop {@link SteamID}.
     */
    public static final long DESKTOP_INSTANCE = 1L;

    /**
     * The account instance value for a console {@link SteamID}.
     */
    public static final long CONSOLE_INSTANCE = 2L;

    /**
     * The account instance for mobile or web based {@link SteamID SteamIDs}.
     */
    public static final long WEB_INSTANCE = 4L;

    /**
     * Masking value used for the account id.
     */
    public static final long ACCOUNT_ID_MASK = 0xFFFFFFFFL;

    /**
     * Masking value used for packing chat instance flags into a {@link SteamID}.
     */
    public static final long ACCOUNT_INSTANCE_MASK = 0x000FFFFFL;

    public SteamID() {
        this(0);
    }

    public SteamID(long unAccountID, EUniverse eUniverse, EAccountType eAccountType) {
        this();
        set(unAccountID, eUniverse, eAccountType);
    }

    /**
     * Initializes a new instance of the {@link SteamID} class.
     *
     * @param unAccountID  The account ID.
     * @param unInstance   The instance.
     * @param eUniverse    The universe.
     * @param eAccountType The account type.
     */
    public SteamID(long unAccountID, long unInstance, EUniverse eUniverse, EAccountType eAccountType) {
        this();
        instancedSet(unAccountID, unInstance, eUniverse, eAccountType);
    }

    /**
     * Initializes a new instance of the {@link SteamID} class.
     *
     * @param id The 64bit integer to assign this SteamID from.
     */
    public SteamID(long id) {
        this.steamID = new BitVector64(id);
    }

    /**
     * Initializes a new instance of the {@link SteamID} class from a Steam2 "STEAM_" rendered form.
     * This constructor assumes the rendered SteamID is in the public universe.
     *
     * @param steamId A "STEAM_" rendered form of the SteamID.
     */
    public SteamID(String steamId) {
        this(steamId, EUniverse.Public);
    }

    /**
     * Initializes a new instance of the {@link SteamID} class from a Steam2 "STEAM_" rendered form and universe.
     *
     * @param steamId   A "STEAM_" rendered form of the SteamID.
     * @param eUniverse The universe the SteamID belongs to.
     */
    public SteamID(String steamId, EUniverse eUniverse) {
        this();
        setFromString(steamId, eUniverse);
    }

    /**
     * Sets the various components of this SteamID instance.
     *
     * @param unAccountID  The account ID.
     * @param eUniverse    The universe.
     * @param eAccountType The account type.
     */
    public void set(long unAccountID, EUniverse eUniverse, EAccountType eAccountType) {
        setAccountID(unAccountID);
        setAccountUniverse(eUniverse);
        setAccountType(eAccountType);

        if (eAccountType == EAccountType.Clan || eAccountType == EAccountType.GameServer) {
            setAccountInstance(0L);
        } else {
            setAccountInstance(DESKTOP_INSTANCE);
        }
    }

    /**
     * Sets the various components of this SteamID instance.
     *
     * @param unAccountID  The account ID.
     * @param unInstance   The instance.
     * @param eUniverse    The universe.
     * @param eAccountType The account type.
     */
    public void instancedSet(long unAccountID, long unInstance, EUniverse eUniverse, EAccountType eAccountType) {
        setAccountID(unAccountID);
        setAccountUniverse(eUniverse);
        setAccountType(eAccountType);
        setAccountInstance(unInstance);
    }

    /**
     * Sets the various components of this SteamID from a Steam2 "STEAM_" rendered form and universe.
     *
     * @param steamId   A "STEAM_" rendered form of the SteamID.
     * @param eUniverse The universe the SteamID belongs to.
     * @return <b>true</b> if this instance was successfully assigned; otherwise, <b>false</b> if the given string was in an invalid format.
     */
    public boolean setFromString(String steamId, EUniverse eUniverse) {
        if (Strings.isNullOrEmpty(steamId)) {
            return false;
        }

        Matcher matcher = STEAM2_REGEX.matcher(steamId);

        if (!matcher.matches()) {
            return false;
        }

        long accountId;
        long authServer;
        try {
            accountId = Long.parseLong(matcher.group(3));
            authServer = Long.parseLong(matcher.group(2));
        } catch (NumberFormatException nfe) {
            return false;
        }

        setAccountUniverse(eUniverse);
        setAccountInstance(1);
        setAccountType(EAccountType.Individual);
        setAccountID((accountId << 1) | authServer);

        return true;
    }

    /**
     * Sets the various components of this SteamID from a Steam3 "[X:1:2:3]" rendered form and universe.
     *
     * @param steamId A "[X:1:2:3]" rendered form of the SteamID.
     * @return <b>true</b> if this instance was successfully assigned; otherwise, <b>false</b> if the given string was in an invalid format.
     */
    public boolean setFromSteam3String(String steamId) {
        if (Strings.isNullOrEmpty(steamId)) {
            return false;
        }

        Matcher matcher = STEAM3_REGEX.matcher(steamId);

        if (!matcher.matches()) {
            matcher = STEAM3_FALLBACK_REGEX.matcher(steamId);

            if (!matcher.matches()) {
                return false;
            }
        }

        long accId;
        long universe;

        try {
            accId = Long.parseLong(matcher.group(3));
            universe = Long.parseLong(matcher.group(2));
        } catch (NumberFormatException nfe) {
            return false;
        }

        String typeString = matcher.group(1);

        if (typeString.length() != 1) {
            return false;
        }

        char type = typeString.charAt(0);

        long instance;

        String instanceGroup = matcher.group(5);
        if (!Strings.isNullOrEmpty(instanceGroup)) {
            instance = Long.parseLong(instanceGroup);
        } else {
            switch (type) {
                case 'g':
                case 'T':
                case 'c':
                case 'L':
                    instance = 0;
                    break;
                default:
                    instance = 1;
                    break;
            }
        }

        if (type == 'c') {
            instance = instance | ChatInstanceFlags.CLAN.code();
            setAccountType(EAccountType.Chat);
        } else if (type == 'L') {
            instance = instance | ChatInstanceFlags.LOBBY.code();
            setAccountType(EAccountType.Chat);
        } else if (type == UNKNOWN_ACCOUNT_TYPE_CHAR) {
            setAccountType(EAccountType.Invalid);
        } else {
            setAccountType(CollectionUtils.getKeyByValue(ACCOUNT_TYPE_CHARS, type));
        }

        setAccountUniverse(EUniverse.from((int) universe));
        setAccountInstance(instance);
        setAccountID(accId);

        return true;
    }

    /**
     * Sets the various components of this SteamID from a 64bit integer form.
     *
     * @param longSteamId The 64bit integer to assign this SteamID from.
     */
    public void setFromUInt64(long longSteamId) {
        this.steamID.setData(longSteamId);
    }

    /**
     * Converts this SteamID into it's 64bit integer form.
     *
     * @return A 64bit integer representing this SteamID.
     */
    public long convertToUInt64() {
        return this.steamID.getData();
    }

    /**
     * Returns a static account key used for grouping accounts with differing instances.
     *
     * @return A 64bit static account key.
     */
    public long getStaticAccountKey() {
        return ((long) getAccountUniverse().code() << 56) + ((long) getAccountType().code() << 52) + getAccountID();
    }

    /**
     * Gets a value indicating whether this instance is a game server account.
     *
     * @return <b>true</b> if this instance is a blank anon account; otherwise, <b>false</b>.
     */
    public boolean isBlankAnonAccount() {
        return getAccountID() == 0 && isAnonAccount() && this.getAccountInstance() == 0;
    }

    /**
     * Gets a value indicating whether this instance is a game server account.
     *
     * @return <b>true</b> if this instance is a game server account; otherwise, <b>false</b>.
     */
    public boolean isGameServerAccount() {
        return getAccountType() == EAccountType.GameServer || getAccountType() == EAccountType.AnonGameServer;
    }

    /**
     * Gets a value indicating whether this instance is a persistent game server account.
     *
     * @return <b>true</b> if this instance is a persistent game server account; otherwise, <b>false</b>.
     */
    public boolean isPersistentGameServerAccount() {
        return getAccountType() == EAccountType.GameServer;
    }

    /**
     * Gets a value indicating whether this instance is an anonymous game server account.
     *
     * @return <b>true</b> if this instance is an anon game server account; otherwise, <b>false</b>.
     */
    public boolean isAnonGameServerAccount() {
        return getAccountType() == EAccountType.AnonGameServer;
    }

    /**
     * Gets a value indicating whether this instance is a content server account.
     *
     * @return <b>true</b> if this instance is a content server account; otherwise, <b>false</b>.
     */
    public boolean isContentServerAccount() {
        return getAccountType() == EAccountType.ContentServer;
    }

    /**
     * Gets a value indicating whether this instance is a clan account.
     *
     * @return <b>true</b> if this instance is a clan account; otherwise, <b>false</b>.
     */
    public boolean isClanAccount() {
        return getAccountType() == EAccountType.Clan;
    }

    /**
     * Gets a value indicating whether this instance is a chat account.
     *
     * @return <b>true</b> if this instance is a chat account; otherwise, <b>false</b>.
     */
    public boolean isChatAccount() {
        return getAccountType() == EAccountType.Chat;
    }

    /**
     * Gets a value indicating whether this instance is a lobby.
     *
     * @return <b>true</b> if this instance is a lobby; otherwise, <b>false</b>.
     */
    public boolean isLobby() {
        return getAccountType() == EAccountType.Chat && (getAccountInstance() & ChatInstanceFlags.LOBBY.code()) > 0;
    }

    /**
     * Gets a value indicating whether this instance is an individual account.
     *
     * @return <b>true</b> if this instance is an individual account; otherwise, <b>false</b>.
     */
    public boolean isIndividualAccount() {
        return getAccountType() == EAccountType.Individual || getAccountType() == EAccountType.ConsoleUser;
    }

    /**
     * Gets a value indicating whether this instance is an anonymous account.
     *
     * @return <b>true</b> if this instance is an anon account; otherwise, <b>false</b>.
     */
    public boolean isAnonAccount() {
        return getAccountType() == EAccountType.AnonUser || getAccountType() == EAccountType.AnonGameServer;
    }

    /**
     * Gets a value indicating whether this instance is an anonymous user account.
     *
     * @return <b>true</b> if this instance is an anon user account; otherwise, <b>false</b>.
     */
    public boolean isAnonUserAccount() {
        return getAccountType() == EAccountType.AnonUser;
    }

    /**
     * Gets a value indicating whether this instance is a console user account.
     *
     * @return <b>true</b> if this instance is a console user account; otherwise, <b>false</b>.
     */
    public boolean isConsoleUserAccount() {
        return getAccountType() == EAccountType.ConsoleUser;
    }

    /**
     * Gets a value indicating whether this instance is valid.
     *
     * @return <b>true</b> if this instance is valid; otherwise, <b>false</b>.
     */
    public boolean isValid() {
        if (getAccountType().code() <= EAccountType.Invalid.code() || getAccountType().code() > EAccountType.AnonUser.code()) {
            return false;
        }

        if (getAccountUniverse().code() <= EUniverse.Invalid.code() || getAccountUniverse().code() > EUniverse.Dev.code()) {
            return false;
        }

        if (getAccountType() == EAccountType.Individual) {
            if (getAccountID() == 0 || getAccountInstance() > WEB_INSTANCE)
                return false;
        }

        if (getAccountType() == EAccountType.Clan) {
            if (getAccountID() == 0 || getAccountInstance() != 0)
                return false;
        }

        if (getAccountType() == EAccountType.GameServer) {
            //noinspection RedundantIfStatement
            if (getAccountID() == 0)
                return false;
        }

        return true;
    }

    public long getAccountID() {
        return steamID.getMask((short) 0, 0xFFFFFFFFL);
    }

    public void setAccountID(long accountID) {
        steamID.setMask((short) 0, 0xFFFFFFFFL, accountID);
    }

    public long getAccountInstance() {
        return steamID.getMask((short) 32, 0xFFFFFL);
    }

    public void setAccountInstance(long accountInstance) {
        steamID.setMask((short) 32, 0xFFFFFL, accountInstance);
    }

    public EAccountType getAccountType() {
        return EAccountType.from((int) steamID.getMask((short) 52, 0xFL));
    }

    public void setAccountType(EAccountType accountType) {
        steamID.setMask((short) 52, 0xFL, accountType == null ? UNKNOWN_ACCOUNT_TYPE_CHAR : accountType.code());
    }

    public EUniverse getAccountUniverse() {
        return EUniverse.from((int) steamID.getMask((short) 56, 0xFFL));
    }

    public void setAccountUniverse(EUniverse accountUniverse) {
        steamID.setMask((short) 56, 0xFFL, accountUniverse.code());
    }

    /**
     * Converts this clan ID to a chat ID.
     *
     * @return The Chat ID for this clan's group chat.
     * @throws IllegalStateException This SteamID is not a clan ID.
     */
    public SteamID toChatID() {
        if (!isClanAccount()) {
            throw new IllegalStateException("Only Clan IDs can be converted to Chat IDs.");
        }

        SteamID chatID = new SteamID(convertToUInt64());

        chatID.setAccountInstance(ChatInstanceFlags.CLAN.code());
        chatID.setAccountType(EAccountType.Chat);

        return chatID;
    }

    /**
     * Converts this chat ID to a clan ID. This can be used to get the group that a group chat is associated with.
     *
     * @return the group that this chat ID is associated with, null if this does not represent a group chat
     */
    public SteamID tryGetClanID() {
        if (isChatAccount() && getAccountInstance() == ChatInstanceFlags.CLAN.code()) {
            SteamID groupID = new SteamID(convertToUInt64());
            groupID.setAccountType(EAccountType.Clan);
            groupID.setAccountInstance(0);
            return groupID;
        }

        return null;
    }

    /**
     * Renders this instance into it's Steam3 representation.
     *
     * @return A string Steam3 representation of this SteamID.
     */
    public String render() {
        return render(true);
    }

    /**
     * Renders this instance into it's Steam2 "STEAM_" or Steam3 representation.
     *
     * @param steam3 If set to <b>true</b>, the Steam3 rendering will be returned; otherwise, the Steam2 STEAM_ rendering.
     * @return A string Steam2 "STEAM_" representation of this SteamID, or a Steam3 representation.
     */
    public String render(boolean steam3) {
        return steam3 ? renderSteam3() : renderSteam2();
    }

    private String renderSteam2() {
        switch (getAccountType()) {
            case Invalid:
            case Individual:
                String universeDigit = (getAccountUniverse().code() <= EUniverse.Public.code()) ? "0" : String.valueOf(getAccountUniverse().code());
                return String.format("STEAM_%s:%d:%d", universeDigit, getAccountID() & 1, getAccountID() >> 1);
            default:
                return String.valueOf(steamID.getData());
        }
    }

    private String renderSteam3() {
        Character accountTypeChar = ACCOUNT_TYPE_CHARS.get(getAccountType());
        if (accountTypeChar == null) {
            accountTypeChar = UNKNOWN_ACCOUNT_TYPE_CHAR;
        }

        if (getAccountType() == EAccountType.Chat) {
            if ((getAccountInstance() & ChatInstanceFlags.CLAN.code()) > 0) {
                accountTypeChar = 'c';
            } else if ((getAccountInstance() & ChatInstanceFlags.LOBBY.code()) > 0) {
                accountTypeChar = 'L';
            }
        }

        boolean renderInstance = false;

        switch (getAccountType()) {
            case AnonGameServer:
            case Multiseat:
                renderInstance = true;
                break;

            case Individual:
                renderInstance = (getAccountInstance() != DESKTOP_INSTANCE);
                break;
        }

        if (renderInstance) {
            return String.format("[%s:%d:%d:%d]", accountTypeChar, getAccountUniverse().code(), getAccountID(), getAccountInstance());
        }

        return String.format("[%s:%d:%d]", accountTypeChar, getAccountUniverse().code(), getAccountID());
    }

    /**
     * Returns a {@link java.lang.String} that represents this instance.
     *
     * @return A {@link java.lang.String} that represents this instance.
     */
    @Override
    public String toString() {
        return render();
    }

    /**
     * Determines whether the specified {@link java.lang.Object} is equal to this instance.
     *
     * @param obj The {@link java.lang.Object} to compare with this instance.
     * @return <b>true</b> if the specified {@link java.lang.Object} is equal to this instance; otherwise, <b>false</b>.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SteamID)) {
            return false;
        }

        SteamID sid = (SteamID) obj;

        return ObjectsCompat.equals(steamID.getData(), sid.steamID.getData());
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code for this instance, suitable for use in hashing algorithms and data structures like a hash table.
     */
    @Override
    public int hashCode() {
        return steamID.getData().hashCode();
    }

    /**
     * Represents various flags a chat {@link SteamID} may have, packed into its instance.
     */
    public enum ChatInstanceFlags {

        /**
         * This flag is set for clan based chat {@link SteamID SteamIDs}.
         */
        CLAN((SteamID.ACCOUNT_INSTANCE_MASK + 1) >> 1),

        /**
         * This flag is set for lobby based chat {@link SteamID SteamIDs}.
         */
        LOBBY((SteamID.ACCOUNT_INSTANCE_MASK + 1) >> 2),

        /**
         * This flag is set for matchmaking lobby based chat {@link SteamID SteamIDs}.
         */
        MMS_LOBBY((SteamID.ACCOUNT_INSTANCE_MASK + 1) >> 3);

        private final long code;

        ChatInstanceFlags(long code) {
            this.code = code;
        }

        public long code() {
            return this.code;
        }

        public static ChatInstanceFlags from(long code) {
            for (ChatInstanceFlags e : ChatInstanceFlags.values()) {
                if (e.code == code) {
                    return e;
                }
            }
            return null;
        }
    }
}
