package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.rpc.IFriendMessages;
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
    public AsyncJobSingle<ServiceMethodResponse> GetRecentMessages(CFriendMessages_GetRecentMessages_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SendMessage(CFriendMessages_SendMessage_Request request) {
        return sendMessage(request);
    }

    @Override
    public void AckMessage(CFriendMessages_AckMessage_Notification request) {
        sendNotification(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> IsInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request) {
        return sendMessage(request);
    }
}
