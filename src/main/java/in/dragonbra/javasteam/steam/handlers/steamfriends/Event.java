package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.GlobalID;

import java.util.Date;

/**
 * Represents an event or announcement that was posted by a clan.
 */
public class Event {
    private GlobalID id;

    private Date eventTime;

    private String headline;

    private GameID gameID;

    private boolean justPosted;

    public Event(CMsgClientClanState.Event clanEvent) {
        id = new GlobalID(clanEvent.getGid());

        eventTime = new Date(clanEvent.getEventTime() * 1000L);
        headline = clanEvent.getHeadline();
        gameID = new GameID(clanEvent.getGameId());

        justPosted = clanEvent.getJustPosted();
    }

    public GlobalID getId() {
        return id;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public String getHeadline() {
        return headline;
    }

    public GameID getGameID() {
        return gameID;
    }

    public boolean isJustPosted() {
        return justPosted;
    }
}
