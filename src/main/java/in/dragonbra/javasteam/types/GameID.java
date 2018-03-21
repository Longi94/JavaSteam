package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.util.compat.ObjectsCompat;

/**
 * This 64bit structure represents an app, mod, shortcut, or p2p file on the Steam network.
 */
public class GameID {

    private BitVector64 gameId;

    /**
     * Initializes a new instance of the {@link GameID} class.
     */
    public GameID() {
        this(0);
    }

    /**
     * Initializes a new instance of the {@link GameID} class.
     *
     * @param id The 64bit integer to assign this GameID from.
     */
    public GameID(long id) {
        gameId = new BitVector64(id);
    }

    /**
     * Initializes a new instance of the {@link GameID} class.
     *
     * @param nAppId The 32bit app id to assign this GameID from.
     */
    public GameID(int nAppId) {
        this((long) nAppId);
    }

    /**
     * Sets the various components of this GameID from a 64bit integer form.
     *
     * @param gameId The 64bit integer to assign this GameID from.
     */
    public void set(long gameId) {
        this.gameId.setData(gameId);
    }

    /**
     * Converts this GameID into it's 64bit integer form.
     *
     * @return A 64bit integer representing this GameID.
     */
    public long toUInt64() {
        return gameId.getData();
    }

    /**
     * Sets the app id.
     *
     * @param value The app ID.
     */
    public void setAppID(int value) {
        gameId.setMask((short) 0, 0xFFFFFFL, value);
    }

    /**
     * Gets the app id.
     *
     * @return The app ID.
     */
    public int getAppID() {
        return (int) gameId.getMask((short) 0, 0xFFFFFFL);
    }

    /**
     * Sets the type of the app.
     *
     * @param value The type of the app.
     */
    public void setAppType(GameType value) {
        gameId.setMask((short) 24, 0xFFL, value.code());
    }

    /**
     * Gets the type of the app.
     *
     * @return The type of the app.
     */
    public GameType getAppType() {
        return GameType.from((int) gameId.getMask((short) 24, 0xFFL));
    }

    /**
     * Sets the mod id.
     *
     * @param value The mod ID.
     */
    public void setModID(long value) {
        gameId.setMask((short) 32, 0xFFFFFFFFL, value);
    }

    /**
     * Gets the mod id.
     *
     * @return The mod ID.
     */
    public long getModID() {
        return gameId.getMask((short) 32, 0xFFFFFFFFL);
    }

    /**
     * Gets a value indicating whether this instance is a mod.
     *
     * @return <b>true</b> if this instance is a mod; otherwise, <b>false</b>.
     */
    public boolean isMod() {
        return getAppType() == GameType.GAME_MOD;
    }

    /**
     * Gets a value indicating whether this instance is a shortcut.
     *
     * @return <b>true</b> if this instance is a shortcut; otherwise, <b>false</b>.
     */
    public boolean isShortcut() {
        return getAppType() == GameType.SHORTCUT;
    }

    /**
     * Gets a value indicating whether this instance is a peer-to-peer file.
     *
     * @return <b>true</b> if this instance is a p2p file; otherwise, <b>false</b>.
     */
    public boolean isP2PFile() {
        return getAppType() == GameType.P2P;
    }

    /**
     * Gets a value indicating whether this instance is a steam app.
     *
     * @return <b>true</b> if this instance is a steam app; otherwise, <b>false</b>.
     */
    public boolean isSteamApp() {
        return getAppType() == GameType.APP;
    }



    /**
     * Sets the various components of this GameID from a 64bit integer form.
     *
     * @param longSteamId The 64bit integer to assign this GameID from.
     */
    public void setFromUInt64(long longSteamId) {
        this.gameId.setData(longSteamId);
    }

    /**
     * Converts this GameID into it's 64bit integer form.
     *
     * @return A 64bit integer representing this GameID.
     */
    public long convertToUInt64() {
        return this.gameId.getData();
    }

    /**
     * Determines whether the specified {@link Object} is equal to this instance.
     *
     * @param obj The {@link Object} to compare with this instance.
     * @return <b>true</b> if the specified {@link Object} is equal to this instance; otherwise, <b>false</b>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof GameID)) {
            return false;
        }

        return ObjectsCompat.equals(gameId.getData(), ((GameID) obj).gameId.getData());
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code for this instance, suitable for use in hashing algorithms and data structures like a hash table.
     */
    @Override
    public int hashCode() {
        return gameId.hashCode();
    }

    /**
     * Returns a {@link String} that represents this instance.
     *
     * @return A {@link String} that represents this instance.
     */
    @Override
    public String toString() {
        return String.valueOf(toUInt64());
    }

    /**
     * Represents various types of games.
     */
    public enum GameType {

        /**
         * A Steam application.
         */
        APP(0),

        /**
         * A game modification.
         */
        GAME_MOD(1),

        /**
         * A shortcut to a program.
         */
        SHORTCUT(2),

        /**
         * A peer-to-peer file.
         */
        P2P(3);

        private final int code;

        GameType(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public static GameType from(int code) {
            for (GameType e : GameType.values()) {
                if (e.code == code) {
                    return e;
                }
            }
            return null;
        }
    }
}
