package `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientOfflineMessageNotification
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Fired in response to calling [SteamNotifications.requestOfflineMessageCount].
 */
class OfflineMessageNotificationCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Get the number of new messages
     */
    val messageCount: Int

    /**
     * Gets the ids of friends the new messages belong to
     */
    val friendsWithOfflineMessages: List<SteamID>

    init {
        val resp = ClientMsgProtobuf<CMsgClientOfflineMessageNotification.Builder>(
            CMsgClientOfflineMessageNotification::class.java,
            packetMsg
        )
        val msg = resp.body

        messageCount = msg.offlineMessages
        friendsWithOfflineMessages = msg.friendsWithOfflineMessagesList.map { SteamID(it.toLong()) }
    }
}
