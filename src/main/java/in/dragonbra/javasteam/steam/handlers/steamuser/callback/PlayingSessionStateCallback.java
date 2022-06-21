package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPlayingSessionState;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received when another client starts or stops playing a game.
 * While {@link PlayingSessionStateCallback#playingBlocked}, sending {@link in.dragonbra.javasteam.enums.EMsg#ClientGamesPlayed}
 * message will log you off with {@link in.dragonbra.javasteam.enums.EResult#LoggedInElsewhere} result.
 */
public class PlayingSessionStateCallback extends CallbackMsg {

    private boolean playingBlocked;

    private int playingAppID;

    public PlayingSessionStateCallback(JobID jobID, CMsgClientPlayingSessionState.Builder msg) {
        setJobID(jobID);

        this.playingBlocked = msg.getPlayingBlocked();
        this.playingAppID = msg.getPlayingApp();
    }

    /**
     * Indicates whether playing is currently blocked by another client.
     *
     * @return true if blocked by another client, otherwise false.
     */
    public boolean isPlayingBlocked() {
        return playingBlocked;
    }

    /**
     * When blocked, gets the appid which is currently being played.
     *
     * @return the app id.
     */
    public int getPlayingAppID() {
        return playingAppID;
    }
}
