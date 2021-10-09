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
     * Gets the AppID of the workshop to enumerate.
     *
     * @return The AppID.
     */
    public int getAppID() {
        return appID;
    }

    /**
     * Sets the AppID of the workshop to enumerate.
     *
     * @param appID The AppID.
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }

    /**
     * Gets the start index.
     *
     * @return The start index.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the start index.
     *
     * @param startIndex The start index.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Gets the user action to filter by.
     * This value is only used by {@link SteamWorkshop#enumeratePublishedFilesByUserAction(EnumerationUserDetails)}
     *
     * @return The user action. See {@link EWorkshopFileAction}
     */
    public EWorkshopFileAction getUserAction() {
        return userAction;
    }

    /**
     * Sets the user action to filter by.
     * This value is only used by {@link SteamWorkshop#enumeratePublishedFilesByUserAction(EnumerationUserDetails)}
     *
     * @param userAction {@link EWorkshopFileAction}
     */
    public void setUserAction(EWorkshopFileAction userAction) {
        this.userAction = userAction;
    }
}
