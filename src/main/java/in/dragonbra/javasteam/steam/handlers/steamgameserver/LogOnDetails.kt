package in.dragonbra.javasteam.steam.handlers.steamgameserver;

/**
 * Represents the details required to log into Steam3 as a game server.
 */
public class LogOnDetails {

    private String token;

    private int appID;

    /**
     * @return the authentication token used to log in as a game server
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the authentication token used to log in as a game server
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the AppID this gameserver will serve
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @param appID the AppID this gameserver will serve
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }
}
