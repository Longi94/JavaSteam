package in.dragonbra.javasteam.steam.handlers.steamworkshop;

import in.dragonbra.javasteam.enums.EWorkshopFileAction;

/**
 * Represents the details of an enumeration request used for the local user's files.
 */
public class EnumerationUserDetails {

    private int appID;

    private int startIndex;

    private EWorkshopFileAction userAction;

    /**
     * @return the AppID of the workshop to enumerate.
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @param appID the AppID of the workshop to enumerate.
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }

    /**
     * @return the start index.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the start index.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the user action to filter by {@link EWorkshopFileAction}.
     * This value is only used by {@link SteamWorkshop#enumeratePublishedFilesByUserAction(EnumerationUserDetails)}
     */
    public EWorkshopFileAction getUserAction() {
        return userAction;
    }

    /**
     * @param userAction the user action to filter by {@link EWorkshopFileAction}.
     *                   This value is only used by {@link SteamWorkshop#enumeratePublishedFilesByUserAction(EnumerationUserDetails)}
     */
    public void setUserAction(EWorkshopFileAction userAction) {
        this.userAction = userAction;
    }
}
