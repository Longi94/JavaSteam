package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.CFriendMessages_MessageReaction_Notification;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IFriendMessagesClient {

    /* NoResponse */
    void IncomingMessage(CFriendMessages_IncomingMessage_Notification request);

    /* NoResponse */
    void NotifyAckMessageEcho(CFriendMessages_AckMessage_Notification request);

    /* NoResponse */
    void MessageReaction(CFriendMessages_MessageReaction_Notification request);
}
