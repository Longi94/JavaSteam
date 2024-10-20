package `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientItemAnnouncements
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * Fired in response to calling [SteamNotifications.requestItemAnnouncements].
 */
class ItemAnnouncementsCallback(packetMsg: IPacketMsg) : CallbackMsg() {
    /**
     * @return the number of new items
     */
    val count: Int

    init {
        val resp = ClientMsgProtobuf<CMsgClientItemAnnouncements.Builder>(
            CMsgClientItemAnnouncements::class.java,
            packetMsg
        )
        val msg = resp.body

        count = msg.countNewItems
    }
}
