package in.dragonbra.javasteam.steam.handlers.steamnotifications.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCommentNotifications;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * Fired in response to calling {@link SteamNotifications#requestCommentNotifications()}.
 */
public class CommentNotificationsCallback extends CallbackMsg {

    private int commentCount;

    private int commentOwnerCount;

    private int commentSubscriptionsCount;

    public CommentNotificationsCallback(CMsgClientCommentNotifications.Builder msg) {
        commentCount = msg.getCountNewComments();
        commentOwnerCount = msg.getCountNewCommentsOwner();
        commentSubscriptionsCount = msg.getCountNewCommentsSubscriptions();
    }

    /**
     * @return the number of new comments
     */
    public int getCommentCount() {
        return commentCount;
    }

    /**
     * @return the number of new comments on the users profile
     */
    public int getCommentOwnerCount() {
        return commentOwnerCount;
    }

    /**
     * @return the number of new comments on subscribed threads
     */
    public int getCommentSubscriptionsCount() {
        return commentSubscriptionsCount;
    }
}
