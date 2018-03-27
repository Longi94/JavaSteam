package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendProfileInfoResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;

import java.util.Date;

/**
 * This callback is fired in response to requesting profile info for a user.
 */
public class ProfileInfoCallback extends CallbackMsg {
    private EResult result;

    private SteamID steamID;

    private Date timeCreated;;

    private String realName;

    private String cityName;

    private String stateName;

    private String countryName;

    private String headline;

    private String summary;

    public ProfileInfoCallback(JobID jobID, CMsgClientFriendProfileInfoResponse.Builder response) {
        setJobID(jobID);

        result = EResult.from(response.getEresult());

        steamID = new SteamID(response.getSteamidFriend());

        timeCreated = new Date(response.getTimeCreated() * 1000L);

        realName = response.getRealName();

        cityName = response.getCityName();
        stateName = response.getStateName();
        countryName = response.getCountryName();

        headline = response.getHeadline();

        summary = response.getSummary();
    }

    public ProfileInfoCallback(EResult result, SteamID steamID, Date timeCreated, String realName, String cityName, String stateName, String countryName, String headline, String summary) {
        this.result = result;
        this.steamID = steamID;
        this.timeCreated = timeCreated;
        this.realName = realName;
        this.cityName = cityName;
        this.stateName = stateName;
        this.countryName = countryName;
        this.headline = headline;
        this.summary = summary;
    }

    public EResult getResult() {
        return result;
    }

    public SteamID getSteamID() {
        return steamID;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public String getRealName() {
        return realName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getHeadline() {
        return headline;
    }

    public String getSummary() {
        return summary;
    }
}
