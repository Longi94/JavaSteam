package in.dragonbra.javasteam.steam.handlers.steamuserstats.callback;

import in.dragonbra.javasteam.enums.ELeaderboardDisplayType;
import in.dragonbra.javasteam.enums.ELeaderboardSortMethod;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSFindOrCreateLBResponse;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to {@link SteamUserStats#findLeaderBoard(int, String)} and
 * {@link SteamUserStats#createLeaderboard(int, String, ELeaderboardSortMethod, ELeaderboardDisplayType)}.
 */
public class FindOrCreateLeaderboardCallback extends CallbackMsg {

    private EResult result;

    private int id;

    private int entryCount;

    private ELeaderboardSortMethod sortMethod;

    private ELeaderboardDisplayType displayType;

    public FindOrCreateLeaderboardCallback(JobID jobID, CMsgClientLBSFindOrCreateLBResponse.Builder resp) {
        setJobID(jobID);

        result = EResult.from(resp.getEresult());
        id = resp.getLeaderboardId();
        entryCount = resp.getLeaderboardEntryCount();
        sortMethod = ELeaderboardSortMethod.from(resp.getLeaderboardSortMethod());
        displayType = ELeaderboardDisplayType.from(resp.getLeaderboardDisplayType());
    }

    public EResult getResult() {
        return result;
    }

    public int getId() {
        return id;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public ELeaderboardSortMethod getSortMethod() {
        return sortMethod;
    }

    public ELeaderboardDisplayType getDisplayType() {
        return displayType;
    }
}
