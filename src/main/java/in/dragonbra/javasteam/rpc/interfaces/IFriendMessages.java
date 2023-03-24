package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IFriendMessages {

    /* CFriendMessages_GetRecentMessages_Response */
    AsyncJobSingle<ServiceMethodResponse> GetRecentMessages(CFriendMessages_GetRecentMessages_Request request);

    /* CFriendsMessages_GetActiveMessageSessions_Response */
    AsyncJobSingle<ServiceMethodResponse> GetActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request);

    /* CFriendMessages_SendMessage_Response */
    AsyncJobSingle<ServiceMethodResponse> SendMessage(CFriendMessages_SendMessage_Request request);

    /* NoResponse */
    void AckMessage(CFriendMessages_AckMessage_Notification request);

    /* CFriendMessages_IsInFriendsUIBeta_Response */
    AsyncJobSingle<ServiceMethodResponse> IsInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request);

    /* CFriendMessages_UpdateMessageReaction_Response */
    AsyncJobSingle<ServiceMethodResponse> UpdateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request);
}
