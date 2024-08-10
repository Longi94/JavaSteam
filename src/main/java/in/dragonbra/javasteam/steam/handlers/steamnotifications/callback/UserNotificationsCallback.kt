package `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUserNotifications
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.Notification
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * Fired when the client receives user notifications.
 */
class UserNotificationsCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the notifications
     */
    val notifications: List<Notification>

    init {
        val resp = ClientMsgProtobuf<CMsgClientUserNotifications.Builder>(
            CMsgClientUserNotifications::class.java,
            packetMsg
        )

        notifications = resp.body.notificationsList.map(::Notification)
    }
}
