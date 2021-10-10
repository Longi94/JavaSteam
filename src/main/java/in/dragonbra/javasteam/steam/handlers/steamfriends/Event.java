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

    /**
     * @return the globally unique ID for this specific event.
     */
    public GlobalID getId() {
        return id;
    }

    /**
     * @return the event time.
     */
    public Date getEventTime() {
        return eventTime;
    }

    /**
     * @return the headline of the event.
     */
    public String getHeadline() {
        return headline;
    }

    /**
     * @return the {@link GameID} associated with this event, if any.
     */
    public GameID getGameID() {
        return gameID;
    }

    /**
     * Gets a value indicating whether this event was just posted.
     *
     * @return <b>true</b> if the event was just posted; otherwise, <b>false</b>.
     */
    public boolean isJustPosted() {
        return justPosted;
    }
}
