package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IFriendMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> getRecentMessages(CFriendMessages_GetRecentMessages_Request request) {
        return sendMessage(request, "GetRecentMessages");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request) {
        return sendMessage(request, "GetActiveMessageSessions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> sendMessage(CFriendMessages_SendMessage_Request request) {
        return sendMessage(request, "SendMessage");
    }

    @Override
    public void ackMessage(CFriendMessages_AckMessage_Notification request) {
        sendNotification(request, "AckMessage");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> isInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request) {
        return sendMessage(request, "IsInFriendsUIBeta");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request) {
        return sendMessage(request, "UpdateMessageReaction");
    }
}
