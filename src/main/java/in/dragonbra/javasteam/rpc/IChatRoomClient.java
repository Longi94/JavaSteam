package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IChatRoomClient {

    /* NoResponse */
    void NotifyIncomingChatMessage(CChatRoom_IncomingChatMessage_Notification request);

    /* NoResponse */
    void NotifyChatMessageModified(CChatRoom_ChatMessageModified_Notification request);

    /* NoResponse */
    void NotifyMemberStateChange(CChatRoom_MemberStateChange_Notification request);

    /* NoResponse */
    void NotifyChatRoomHeaderStateChange(CChatRoom_ChatRoomHeaderState_Notification request);

    /* NoResponse */
    void NotifyChatRoomGroupRoomsChange(CChatRoom_ChatRoomGroupRoomsChange_Notification request);

    /* NoResponse */
    void NotifyShouldRejoinChatRoomVoiceChat(CChatRoom_NotifyShouldRejoinChatRoomVoiceChat_Notification request);

    /* NoResponse */
    void NotifyChatGroupUserStateChanged(ChatRoomClient_NotifyChatGroupUserStateChanged_Notification request);

    /* NoResponse */
    void NotifyAckChatMessageEcho(CChatRoom_AckChatMessage_Notification request);

    /* NoResponse */
    void NotifyChatRoomDisconnect(ChatRoomClient_NotifyChatRoomDisconnect_Notification request);

    /* NoResponse */
    void NotifyMemberListViewUpdated(CChatRoomClient_MemberListViewUpdated_Notification request);

    /* NoResponse */
    void NotifyMessageReaction(CChatRoom_MessageReaction_Notification request);
}
