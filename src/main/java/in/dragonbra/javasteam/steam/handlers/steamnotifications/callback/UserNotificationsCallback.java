package in.dragonbra.javasteam.steam.handlers.steamnotifications.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUserNotifications;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.Notification;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Fired when the client receives user notifications.
 */
public class UserNotificationsCallback extends CallbackMsg {

    private final List<Notification> notifications;

    public UserNotificationsCallback(CMsgClientUserNotifications.Builder msg) {
        notifications = new ArrayList<>();
        for (CMsgClientUserNotifications.Notification notification : msg.getNotificationsList()) {
            notifications.add(new Notification(notification));
        }
    }

    /**
     * @return the notifications
     */
    public List<Notification> getNotifications() {
        return notifications;
    }
}
