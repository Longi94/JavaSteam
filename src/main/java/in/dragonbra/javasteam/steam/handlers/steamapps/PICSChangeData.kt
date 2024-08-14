package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceResponse;

/**
 * Holds the change data for a single app or package
 */
public class PICSChangeData {

    private final int id;

    private final int changeNumber;

    private final boolean needsToken;

    public PICSChangeData(CMsgClientPICSChangesSinceResponse.AppChange change) {
        id = change.getAppid();
        changeNumber = change.getChangeNumber();
        needsToken = change.getNeedsToken();
    }

    public PICSChangeData(CMsgClientPICSChangesSinceResponse.PackageChange change) {
        id = change.getPackageid();
        changeNumber = change.getChangeNumber();
        needsToken = change.getNeedsToken();
    }

    /**
     * @return the app or package ID this change data represents
     */
    public int getId() {
        return id;
    }

    /**
     * @return the current change number of this app
     */
    public int getChangeNumber() {
        return changeNumber;
    }

    /**
     * @return signals if an access token is needed for this request
     */
    public boolean isNeedsToken() {
        return needsToken;
    }
}
