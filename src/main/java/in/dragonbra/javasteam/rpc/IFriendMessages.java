package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.NoResponse;

public interface IFriendMessages {
    CFriendMessages_GetRecentMessages_Response GetRecentMessages(CFriendMessages_GetRecentMessages_Request request);

    CFriendsMessages_GetActiveMessageSessions_Response GetActiveMessageSessions(CFriendsMessages_GetActiveMessageSessions_Request request);

    CFriendMessages_SendMessage_Response SendMessage(CFriendMessages_SendMessage_Request request);

    NoResponse AckMessage(CFriendMessages_AckMessage_Notification request);

    CFriendMessages_IsInFriendsUIBeta_Response IsInFriendsUIBeta(CFriendMessages_IsInFriendsUIBeta_Request request);

    CFriendMessages_UpdateMessageReaction_Response UpdateMessageReaction(CFriendMessages_UpdateMessageReaction_Request request);
}
