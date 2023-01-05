package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.rpc.IFriendMessagesClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class FriendMessagesClient extends UnifiedService implements IFriendMessagesClient {

    public FriendMessagesClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void IncomingMessage(CFriendMessages_IncomingMessage_Notification request) {
        sendNotification(CFriendMessages_IncomingMessage_Notification.class, request.toBuilder());
    }

    @Override
    public void NotifyAckMessageEcho(CFriendMessages_AckMessage_Notification request) {
        sendNotification(CFriendMessages_AckMessage_Notification.class, request.toBuilder());
    }

    @Override
    public void MessageReaction(CFriendMessages_MessageReaction_Notification request) {
        sendNotification(CFriendMessages_MessageReaction_Notification.class, request.toBuilder());
    }
}
