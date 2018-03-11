package in.dragonbra.javasteam.steam.handlers.steamgameserver;

import in.dragonbra.javasteam.enums.EServerFlags;

import java.net.InetAddress;
import java.util.EnumSet;

/**
 * Represents the details of the game server's current status.
 */
public class StatusDetails {

    private int appID;

    private EnumSet<EServerFlags> serverFlags;

    private String gameDirectory;

    private InetAddress address;

    private int port;

    private int queryPort;

    private String version;

    /**
     * @return the AppID this game server is serving
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @param appID the AppID this game server is serving
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }

    /**
     * @return the server's basic state as flags
     */
    public EnumSet<EServerFlags> getServerFlags() {
        return serverFlags;
    }

    /**
     * @param serverFlags the server's basic state as flags
     */
    public void setServerFlags(EnumSet<EServerFlags> serverFlags) {
        this.serverFlags = serverFlags;
    }

    /**
     * @return the directory the game data is in
     */
    public String getGameDirectory() {
        return gameDirectory;
    }

    /**
     * @param gameDirectory the directory the game data is in
     */
    public void setGameDirectory(String gameDirectory) {
        this.gameDirectory = gameDirectory;
    }

    /**
     * @return the IP address the game server listens on
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * @param address the IP address the game server listens on
     */
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    /**
     * @return the port the game server listens on
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port the game server listens on
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the port the game server responds to queries on
     */
    public int getQueryPort() {
        return queryPort;
    }

    /**
     * @param queryPort the port the game server responds to queries on
     */
    public void setQueryPort(int queryPort) {
        this.queryPort = queryPort;
    }

    /**
     * @return the current version of the game server
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the current version of the game server
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
