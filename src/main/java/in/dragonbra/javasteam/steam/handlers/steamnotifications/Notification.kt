package `in`.dragonbra.javasteam.steam.handlers.steamnotifications

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUserNotifications

/**
 * Represents a notification.
 */
class Notification(notification: CMsgClientUserNotifications.Notification) {
    /**
     * @return the number of notifications
     */
    val count: Int = notification.count

    /**
     * @return the type of the notification
     */
    val type: Int = notification.userNotificationType
}
