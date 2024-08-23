package `in`.dragonbra.javasteam.steam.handlers.steamnotifications

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestCommentNotifications
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestItemAnnouncements
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestOfflineMessageCount
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback.CommentNotificationsCallback
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback.ItemAnnouncementsCallback
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback.OfflineMessageNotificationCallback
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback.UserNotificationsCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This handler handles steam notifications.
 */
class SteamNotifications : ClientMsgHandler() {

    /**
     * Request comment notifications.
     * Results are returned in a [CommentNotificationsCallback].
     */
    fun requestCommentNotifications() {
        ClientMsgProtobuf<CMsgClientRequestCommentNotifications.Builder>(
            CMsgClientRequestCommentNotifications::class.java,
            EMsg.ClientRequestCommentNotifications
        ).also(client::send)
    }

    /**
     * Request new items notifications.
     * Results are returned in a [ItemAnnouncementsCallback].
     */
    fun requestItemAnnouncements() {
        ClientMsgProtobuf<CMsgClientRequestItemAnnouncements.Builder>(
            CMsgClientRequestItemAnnouncements::class.java,
            EMsg.ClientRequestItemAnnouncements
        ).also(client::send)
    }

    /**
     * Request offline message count.
     * Results are returned in a [OfflineMessageNotificationCallback].
     */
    fun requestOfflineMessageCount() {
        ClientMsgProtobuf<CMsgClientRequestOfflineMessageCount.Builder>(
            CMsgClientRequestOfflineMessageCount::class.java,
            EMsg.ClientChatRequestOfflineMessageCount
        ).also(client::send)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientUserNotifications -> UserNotificationsCallback(packetMsg)
            EMsg.ClientChatOfflineMessageNotification -> OfflineMessageNotificationCallback(packetMsg)
            EMsg.ClientCommentNotifications -> CommentNotificationsCallback(packetMsg)
            EMsg.ClientItemAnnouncements -> ItemAnnouncementsCallback(packetMsg)
            else -> null
        }
    }
}
