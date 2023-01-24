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

    private final EResult result;

    private final int id;

    private final int entryCount;

    private final ELeaderboardSortMethod sortMethod;

    private final ELeaderboardDisplayType displayType;

    public FindOrCreateLeaderboardCallback(JobID jobID, CMsgClientLBSFindOrCreateLBResponse.Builder resp) {
        setJobID(jobID);

        result = EResult.from(resp.getEresult());
        id = resp.getLeaderboardId();
        entryCount = resp.getLeaderboardEntryCount();
        sortMethod = ELeaderboardSortMethod.from(resp.getLeaderboardSortMethod());
        displayType = ELeaderboardDisplayType.from(resp.getLeaderboardDisplayType());
    }

    /**
     * @return the result of the request by {@link EResult}.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the leaderboard ID.
     */
    public int getId() {
        return id;
    }

    /**
     * @return how many entries there are for requested leaderboard.
     */
    public int getEntryCount() {
        return entryCount;
    }

    /**
     * @return sort method to use for this leaderboard. See {@link ELeaderboardSortMethod}.
     */
    public ELeaderboardSortMethod getSortMethod() {
        return sortMethod;
    }

    /**
     * @return display type for this leaderboard. See {@link ELeaderboardDisplayType}
     */
    public ELeaderboardDisplayType getDisplayType() {
        return displayType;
    }
}
