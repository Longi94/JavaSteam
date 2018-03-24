package in.dragonbra.javasteam.steam.handlers.steamnotifications.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientOfflineMessageNotification;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fired in response to calling {@link SteamNotifications#requestOfflineMessageCount()}.
 */
public class OfflineMessageNotificationCallback extends CallbackMsg {

    private int messageCount;

    private List<SteamID> friendsWithOfflineMessages;

    public OfflineMessageNotificationCallback(CMsgClientOfflineMessageNotification.Builder msg) {
        messageCount = msg.getOfflineMessages();

        friendsWithOfflineMessages = new ArrayList<>();
        for (Integer id : msg.getFriendsWithOfflineMessagesList()) {
            friendsWithOfflineMessages.add(new SteamID(id));
        }
        friendsWithOfflineMessages = Collections.unmodifiableList(friendsWithOfflineMessages);
    }

    /**
     * @return the number of new messages
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * @return the ids of friends the new messages belong to
     */
    public List<SteamID> getFriendsWithOfflineMessages() {
        return friendsWithOfflineMessages;
    }
}
