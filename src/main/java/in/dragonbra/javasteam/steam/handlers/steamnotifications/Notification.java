package in.dragonbra.javasteam.steam.handlers.steamnotifications;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUserNotifications;

/**
 * Represents a notification.
 */
public class Notification {

    private int count;

    private int type;

    public Notification(CMsgClientUserNotifications.Notification notification) {
        count = notification.getCount();
        type = notification.getUserNotificationType();
    }

    /**
     * @return the number of notifications
     */
    public int getCount() {
        return count;
    }

    /**
     * @return the type of the notification
     */
    public int getType() {
        return type;
    }
}
