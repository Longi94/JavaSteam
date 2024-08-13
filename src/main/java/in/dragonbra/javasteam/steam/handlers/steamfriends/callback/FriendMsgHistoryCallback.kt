package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryResponse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.FriendMessage;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This callback is fired in response to receiving historical messages.
 *
 * @see SteamFriends#requestOfflineMessages()
 * @see SteamFriends#requestMessageHistory(SteamID)
 */
public class FriendMsgHistoryCallback extends CallbackMsg {

    private final EResult result;

    private final SteamID steamID;

    private final List<FriendMessage> messages;

    public FriendMsgHistoryCallback(CMsgClientChatGetFriendMessageHistoryResponse.Builder msg, EUniverse universe) {
        result = EResult.from(msg.getSuccess());

        steamID = new SteamID(msg.getSteamid());

        List<FriendMessage> messages = new ArrayList<>();
        for (CMsgClientChatGetFriendMessageHistoryResponse.FriendMessage m : msg.getMessagesList()) {
            SteamID senderID = new SteamID(m.getAccountid(), universe, EAccountType.Individual);
            Date timestamp = new Date(m.getTimestamp() * 1000L);

            messages.add(new FriendMessage(senderID, m.getUnread(), m.getMessage(), timestamp));
        }

        this.messages = Collections.unmodifiableList(messages);
    }

    /**
     * @return the result of the request.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the {@link SteamID} of the user with whom these messages were exchanged.
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * Offline messages are marked by having set {@link FriendMessage#isUnread()} to <b>true</b>
     *
     * @return the messages exchanged with the user.
     */
    public List<FriendMessage> getMessages() {
        return messages;
    }
}
