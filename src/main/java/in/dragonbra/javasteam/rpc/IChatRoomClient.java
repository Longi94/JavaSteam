package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IChatRoomClient {
    NoResponse NotifyIncomingChatMessage(CChatRoom_IncomingChatMessage_Notification request);

    NoResponse NotifyChatMessageModified(CChatRoom_ChatMessageModified_Notification request);

    NoResponse NotifyMemberStateChange(CChatRoom_MemberStateChange_Notification request);

    NoResponse NotifyChatRoomHeaderStateChange(CChatRoom_ChatRoomHeaderState_Notification request);

    NoResponse NotifyChatRoomGroupRoomsChange(CChatRoom_ChatRoomGroupRoomsChange_Notification request);

    NoResponse NotifyShouldRejoinChatRoomVoiceChat(CChatRoom_NotifyShouldRejoinChatRoomVoiceChat_Notification request);

    NoResponse NotifyChatGroupUserStateChanged(ChatRoomClient_NotifyChatGroupUserStateChanged_Notification request);

    NoResponse NotifyAckChatMessageEcho(CChatRoom_AckChatMessage_Notification request);

    NoResponse NotifyChatRoomDisconnect(ChatRoomClient_NotifyChatRoomDisconnect_Notification request);

    NoResponse NotifyMemberListViewUpdated(CChatRoomClient_MemberListViewUpdated_Notification request);

    NoResponse NotifyMessageReaction(CChatRoom_MessageReaction_Notification request);
}
