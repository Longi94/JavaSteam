package `in`.dragonbra.javasteam.steam.handlers.steamnotifications.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCommentNotifications
import `in`.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * Fired in response to calling [SteamNotifications.requestCommentNotifications].
 */
class CommentNotificationsCallback(packetMsg: IPacketMsg) : CallbackMsg() {
    /**
     * @return the number of new comments
     */
    val commentCount: Int

    /**
     * @return the number of new comments on the users profile
     */
    val commentOwnerCount: Int

    /**
     * @return the number of new comments on subscribed threads
     */
    val commentSubscriptionsCount: Int

    init {
        val resp = ClientMsgProtobuf<CMsgClientCommentNotifications.Builder>(
            CMsgClientCommentNotifications::class.java,
            packetMsg
        )

        commentCount = resp.body.countNewComments
        commentOwnerCount = resp.body.countNewCommentsOwner
        commentSubscriptionsCount = resp.body.countNewCommentsSubscriptions
    }
}
