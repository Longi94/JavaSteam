package in.dragonbra.javasteam.steam.handlers.steamworkshop;

import in.dragonbra.javasteam.enums.EWorkshopFileAction;

/**
 * Represents the details of an enumeration request used for the local user's files.
 */
public class EnumerationUserDetails {

    private int appID;

    private int sortOrder;

    private int startIndex;

    private EWorkshopFileAction userAction;

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public EWorkshopFileAction getUserAction() {
        return userAction;
    }

    public void setUserAction(EWorkshopFileAction userAction) {
        this.userAction = userAction;
    }
}
