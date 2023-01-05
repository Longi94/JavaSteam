package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IFriendMessages {

    /* CFriendMessages_GetRecentMessages_Response */
    JobID GetRecentMessages(CFriendMessages_GetRecentMessages_Request request);

    CFriendsMessages_GetActiveMessageSessions_Response GetActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request);

    /* CFriendMessages_SendMessage_Response */
    JobID SendMessage(CFriendMessages_SendMessage_Request request);

    /* NoResponse */
    void AckMessage(CFriendMessages_AckMessage_Notification request);

    /* CFriendMessages_IsInFriendsUIBeta_Response */
    JobID IsInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request);

    /* CFriendMessages_UpdateMessageReaction_Response */
    JobID UpdateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request);
}
