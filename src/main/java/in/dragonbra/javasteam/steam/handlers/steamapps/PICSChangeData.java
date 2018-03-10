package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientPICSChangesSinceResponse;

/**
 * Holds the change data for a single app or package
 */
public class PICSChangeData {

    private int id;

    private int changeNumber;

    private boolean needsToken;

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

    public int getId() {
        return id;
    }

    public int getChangeNumber() {
        return changeNumber;
    }

    public boolean isNeedsToken() {
        return needsToken;
    }
}
