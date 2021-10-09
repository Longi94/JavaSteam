package in.dragonbra.javasteam.steam.handlers.steamscreenshots;

import in.dragonbra.javasteam.enums.EUCMFilePrivacyState;
import in.dragonbra.javasteam.types.GameID;

import java.util.Date;

/**
 * Represents the details required to add a screenshot
 */
public class ScreenshotDetails {

    private GameID gameID;

    private String ufsImageFilePath;

    private String usfThumbnailFilePath;

    private String caption;

    private EUCMFilePrivacyState privacy;

    private int width;

    private int height;

    private Date creationTime;

    private boolean containsSpoilers;

    /**
     * @return the Steam game ID this screenshot belongs to
     */
    public GameID getGameID() {
        return gameID;
    }

    /**
     * @param gameID the Steam game ID this screenshot belongs to
     */
    public void setGameID(GameID gameID) {
        this.gameID = gameID;
    }

    /**
     * @return the UFS image filepath.
     */
    public String getUfsImageFilePath() {
        return ufsImageFilePath;
    }

    /**
     * @param ufsImageFilePath the UFS image filepath.
     */
    public void setUfsImageFilePath(String ufsImageFilePath) {
        this.ufsImageFilePath = ufsImageFilePath;
    }

    /**
     * @return the UFS thumbnail filepath.
     */
    public String getUsfThumbnailFilePath() {
        return usfThumbnailFilePath;
    }

    /**
     * @param usfThumbnailFilePath the UFS thumbnail filepath.
     */
    public void setUsfThumbnailFilePath(String usfThumbnailFilePath) {
        this.usfThumbnailFilePath = usfThumbnailFilePath;
    }

    /**
     * @return the screenshot caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the screenshot caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the screenshot privacy
     */
    public EUCMFilePrivacyState getPrivacy() {
        return privacy;
    }

    /**
     * @param privacy the screenshot privacy
     */
    public void setPrivacy(EUCMFilePrivacyState privacy) {
        this.privacy = privacy;
    }

    /**
     * @return the screenshot width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the screenshot width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the screenshot height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the screenshot height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the creation time
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime the creation time
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return whether or not the screenshot contains spoilers
     */
    public boolean isContainsSpoilers() {
        return containsSpoilers;
    }

    /**
     * @param containsSpoilers whether or not the screenshot contains spoilers.
     */
    public void setContainsSpoilers(boolean containsSpoilers) {
        this.containsSpoilers = containsSpoilers;
    }
}
