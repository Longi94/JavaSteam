package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IChatRoom {

    /* CChatRoom_CreateChatRoomGroup_Response */
    JobID CreateChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request);

    /* CChatRoom_SaveChatRoomGroup_Response */
    JobID SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request);

    /* CChatRoom_RenameChatRoomGroup_Response */
    JobID RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request);

    /* CChatRoom_SetChatRoomGroupTagline_Response */
    JobID SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request);

    /* CChatRoom_SetChatRoomGroupAvatar_Response */
    JobID SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request);

    /* CChatRoom_SetChatRoomGroupWatchingBroadcast_Response */
    JobID SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request);

    /* CChatRoom_JoinMiniGameForChatRoomGroup_Response */
    JobID JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request);

    /* CChatRoom_EndMiniGameForChatRoomGroup_Response */
    JobID EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request);

    /* CChatRoom_MuteUser_Response */
    JobID MuteUserInGroup(CChatRoom_MuteUser_Request request);

    /* CChatRoom_KickUser_Response */
    JobID KickUserFromGroup(CChatRoom_KickUser_Request request);

    /* CChatRoom_SetUserBanState_Response */
    JobID SetUserBanState(CChatRoom_SetUserBanState_Request request);

    /* CChatRoom_RevokeInvite_Response */
    JobID RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request);

    /* CChatRoom_CreateRole_Response */
    JobID CreateRole(CChatRoom_CreateRole_Request request);

    /* CChatRoom_GetRoles_Response */
    JobID GetRoles(CChatRoom_GetRoles_Request request);

    /* CChatRoom_RenameRole_Response */
    JobID RenameRole(CChatRoom_RenameRole_Request request);

    /* CChatRoom_ReorderRole_Response */
    JobID ReorderRole(CChatRoom_ReorderRole_Request request);

    /* CChatRoom_DeleteRole_Response */
    JobID DeleteRole(CChatRoom_DeleteRole_Request request);

    /* CChatRoom_GetRoleActions_Response */
    JobID GetRoleActions(CChatRoom_GetRoleActions_Request request);

    /* CChatRoom_ReplaceRoleActions_Response */
    JobID ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request);

    /* CChatRoom_AddRoleToUser_Response */
    JobID AddRoleToUser(CChatRoom_AddRoleToUser_Request request);

    /* CChatRoom_GetRolesForUser_Response */
    JobID GetRolesForUser(CChatRoom_GetRolesForUser_Request request);

    /* CChatRoom_DeleteRoleFromUser_Response */
    JobID DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request);

    /* CChatRoom_JoinChatRoomGroup_Response */
    JobID JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request);

    /* CChatRoom_InviteFriendToChatRoomGroup_Response */
    JobID InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request);

    /* CChatRoom_LeaveChatRoomGroup_Response */
    JobID LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request);

    /* CChatRoom_CreateChatRoom_Response */
    JobID CreateChatRoom(CChatRoom_CreateChatRoom_Request request);

    /* CChatRoom_DeleteChatRoom_Response */
    JobID DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request);

    /* CChatRoom_RenameChatRoom_Response */
    JobID RenameChatRoom(CChatRoom_RenameChatRoom_Request request);

    /* CChatRoom_ReorderChatRoom_Response */
    JobID ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request);

    /* CChatRoom_SendChatMessage_Response */
    JobID SendChatMessage(CChatRoom_SendChatMessage_Request request);

    /* CChatRoom_JoinVoiceChat_Response */
    JobID JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request);

    /* CChatRoom_LeaveVoiceChat_Response */
    JobID LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request);

    /* CChatRoom_GetMessageHistory_Response */
    JobID GetMessageHistory(CChatRoom_GetMessageHistory_Request request);

    /* CChatRoom_GetMyChatRoomGroups_Response */
    JobID GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request);

    /* CChatRoom_GetChatRoomGroupState_Response */
    JobID GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request);

    /* CChatRoom_GetChatRoomGroupSummary_Response */
    JobID GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request);

    /* CChatRoom_SetAppChatRoomGroupForceActive_Response */
    JobID SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request);

    /* NoResponse */
    void SetAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request);

    /* NoResponse */
    void AckChatMessage(CChatRoom_AckChatMessage_Notification request);

    /* CChatRoom_CreateInviteLink_Response */
    JobID CreateInviteLink(CChatRoom_CreateInviteLink_Request request);

    /* CChatRoom_GetInviteLinkInfo_Response */
    JobID GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request);

    /* CChatRoom_GetInviteInfo_Response */
    JobID GetInviteInfo(CChatRoom_GetInviteInfo_Request request);

    /* CChatRoom_GetInviteLinksForGroup_Response */
    JobID GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request);

    /* CChatRoom_GetBanList_Response */
    JobID GetBanList(CChatRoom_GetBanList_Request request);

    /*CChatRoom_GetInviteList_Response */
    JobID GetInviteList(CChatRoom_GetInviteList_Request request);

    /* CChatRoom_DeleteInviteLink_Response */
    JobID DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request);

    /* CChatRoom_SetSessionActiveChatRoomGroups_Response */
    JobID SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request);

    /* CChatRoom_SetUserChatGroupPreferences_Response */
    JobID SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request);

    /* CChatRoom_DeleteChatMessages_Response */
    JobID DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request);

    /* NoResponse */
    void UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request);

    /* CChatRoom_SearchMembers_Response */
    JobID SearchMembers(CChatRoom_SearchMembers_Request request);

    /* CChatRoom_UpdateMessageReaction_Response */
    JobID UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request);

    /* CChatRoom_GetMessageReactionReactors_Response */
    JobID GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request);
}
