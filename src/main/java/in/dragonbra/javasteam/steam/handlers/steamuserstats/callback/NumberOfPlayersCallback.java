package in.dragonbra.javasteam.steam.handlers.steamuserstats.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgDPGetNumberOfCurrentPlayersResponse;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to {@link SteamUserStats#getNumberOfCurrentPlayers(int)}.
 */
public class NumberOfPlayersCallback extends CallbackMsg {

    private EResult result;

    private int numPlayers;

    public NumberOfPlayersCallback(JobID jobID, CMsgDPGetNumberOfCurrentPlayersResponse.Builder resp) {
        setJobID(jobID);
        result = EResult.from(resp.getEresult());
        numPlayers = resp.getPlayerCount();
    }

    /**
     * @return the result of the request by {@link EResult}.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the current number of players according to Steam.
     */
    public int getNumPlayers() {
        return numPlayers;
    }
}
