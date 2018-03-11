package in.dragonbra.javasteam.steam.handlers.steamworkshop;

import in.dragonbra.javasteam.enums.EWorkshopEnumerationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the details of an enumeration request for all published files.
 */
public class EnumerationDetails {

    private int appID;

    private EWorkshopEnumerationType type;

    private int startIndex;

    private int days;

    private int count;

    private List<String> tags = new ArrayList<>();

    private List<String> userTags = new ArrayList<>();

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public EWorkshopEnumerationType getType() {
        return type;
    }

    public void setType(EWorkshopEnumerationType type) {
        this.type = type;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getUserTags() {
        return userTags;
    }
}
