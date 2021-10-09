package in.dragonbra.javasteam.steam.handlers.steamuserstats.callback;

import in.dragonbra.javasteam.enums.ELeaderboardDataRequest;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntriesResponse;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.LeaderboardEntry;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is fired in response to {@link SteamUserStats#getLeaderboardEntries(int, int, int, int, ELeaderboardDataRequest)}.
 */
public class LeaderboardEntriesCallback extends CallbackMsg {

    private EResult result;

    private int entryCount;

    private List<LeaderboardEntry> entries;

    public LeaderboardEntriesCallback(JobID jobID, CMsgClientLBSGetLBEntriesResponse.Builder resp) {
        setJobID(jobID);

        result = EResult.from(resp.getEresult());
        entryCount = resp.getLeaderboardEntryCount();

        List<LeaderboardEntry> list = new ArrayList<>();

        for (CMsgClientLBSGetLBEntriesResponse.Entry entry : resp.getEntriesList()) {
            list.add(new LeaderboardEntry(entry));
        }

        entries = Collections.unmodifiableList(list);
    }

    /**
     * Gets the result of the request.
     *
     * @return The result by {@link EResult}
     */
    public EResult getResult() {
        return result;
    }

    /**
     * How many entries there are for requested leaderboard.
     *
     * @return The number of entries.
     */
    public int getEntryCount() {
        return entryCount;
    }

    /**
     * Gets the list of leaderboard entries this response contains.
     *
     * @return The list of leaderboard entries. {@link LeaderboardEntry}
     */
    public List<LeaderboardEntry> getEntries() {
        return entries;
    }
}
