package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.types.SteamID;

import java.util.Date;

/**
 * Represents a single Message sent to or received from a friend
 */
public class FriendMessage {

    private SteamID steamID;

    private boolean unread;

    private String message;

    private Date timestamp;

    public FriendMessage(SteamID steamID, boolean unread, String message, Date timestamp) {
        this.steamID = steamID;
        this.unread = unread;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * @return the {@link SteamID} of the User that wrote the message
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return whether or not the message has been read, i.e., is an offline message.
     */
    public boolean isUnread() {
        return unread;
    }

    /**
     * @return the actual message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return time (in UTC) when the message was sent
     */
    public Date getTimestamp() {
        return timestamp;
    }
}
