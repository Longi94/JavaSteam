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

    public SteamID getSteamID() {
        return steamID;
    }

    public boolean isUnread() {
        return unread;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
