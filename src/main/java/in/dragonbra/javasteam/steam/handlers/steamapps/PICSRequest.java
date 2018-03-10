package in.dragonbra.javasteam.steam.handlers.steamapps;

/**
 * Represents a PICS request used for {@link SteamApps#picsGetProductInfo(Iterable, Iterable, boolean)}
 */
public class PICSRequest {

    private int id;

    private long accessToken;

    private boolean _public;

    /**
     * Instantiate a PICS product info request
     */
    public PICSRequest() {
        this(0, 0L, true);
    }

    /**
     * Instantiate a PICS product info request for a given app or package id
     *
     * @param id App or package ID\
     */
    public PICSRequest(int id) {
        this(id, 0L, true);
    }

    /**
     * Instantiate a PICS product info request for a given app or package id and an access token
     *
     * @param id          App or package ID
     * @param accessToken PICS access token
     * @param _public     Get only public info
     */
    public PICSRequest(int id, long accessToken, boolean _public) {
        this.id = id;
        this.accessToken = accessToken;
        this._public = _public;
    }

    /**
     * @return the ID of the app or package being requested
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the ID of the app or package being requested
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the access token associated with the request
     */
    public long getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the access token associated with the request
     */
    public void setAccessToken(long accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return The flag specifying if only public data is requested
     */
    public boolean isPublic() {
        return _public;
    }

    /**
     * @param _public The flag specifying if only public data is requested
     */
    public void setPublic(boolean _public) {
        this._public = _public;
    }
}
