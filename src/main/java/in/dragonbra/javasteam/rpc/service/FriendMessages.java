package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.rpc.IFriendMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class FriendMessages extends UnifiedService implements IFriendMessages {

    public FriendMessages(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetRecentMessages(CFriendMessages_GetRecentMessages_Request request) {
        return sendMessage(CFriendMessages_GetRecentMessages_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request) {
        return sendMessage(CFriendsMessages_GetActiveMessageSessions_Request.class, request.toBuilder());
    }

    @Override
    public JobID SendMessage(CFriendMessages_SendMessage_Request request) {
        return sendMessage(CFriendMessages_SendMessage_Request.class, request.toBuilder());
    }

    @Override
    public void AckMessage(CFriendMessages_AckMessage_Notification request) {
        sendNotification(CFriendMessages_AckMessage_Notification.class, request.toBuilder());
    }

    @Override
    public JobID IsInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request) {
        return sendMessage(CFriendMessages_IsInFriendsUIBeta_Request.class, request.toBuilder());
    }

    @Override
    public JobID UpdateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request) {
        return sendMessage(CFriendMessages_UpdateMessageReaction_Request.class, request.toBuilder());
    }
}
