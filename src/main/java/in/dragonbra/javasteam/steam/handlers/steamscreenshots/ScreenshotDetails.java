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

    public GameID getGameID() {
        return gameID;
    }

    public void setGameID(GameID gameID) {
        this.gameID = gameID;
    }

    public String getUfsImageFilePath() {
        return ufsImageFilePath;
    }

    public void setUfsImageFilePath(String ufsImageFilePath) {
        this.ufsImageFilePath = ufsImageFilePath;
    }

    public String getUsfThumbnailFilePath() {
        return usfThumbnailFilePath;
    }

    public void setUsfThumbnailFilePath(String usfThumbnailFilePath) {
        this.usfThumbnailFilePath = usfThumbnailFilePath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public EUCMFilePrivacyState getPrivacy() {
        return privacy;
    }

    public void setPrivacy(EUCMFilePrivacyState privacy) {
        this.privacy = privacy;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isContainsSpoilers() {
        return containsSpoilers;
    }

    public void setContainsSpoilers(boolean containsSpoilers) {
        this.containsSpoilers = containsSpoilers;
    }
}
