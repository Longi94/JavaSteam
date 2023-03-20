package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IChatRoom {

    /* CChatRoom_CreateChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request);

    /* CChatRoom_SaveChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request);

    /* CChatRoom_RenameChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request);

    /* CChatRoom_SetChatRoomGroupTagline_Response */
    AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request);

    /* CChatRoom_SetChatRoomGroupAvatar_Response */
    AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request);

    /* CChatRoom_SetChatRoomGroupWatchingBroadcast_Response */
    AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request);

    /* CChatRoom_JoinMiniGameForChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request);

    /* CChatRoom_EndMiniGameForChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request);

    /* CChatRoom_MuteUser_Response */
    AsyncJobSingle<ServiceMethodResponse> MuteUserInGroup(CChatRoom_MuteUser_Request request);

    /* CChatRoom_KickUser_Response */
    AsyncJobSingle<ServiceMethodResponse> KickUserFromGroup(CChatRoom_KickUser_Request request);

    /* CChatRoom_SetUserBanState_Response */
    AsyncJobSingle<ServiceMethodResponse> SetUserBanState(CChatRoom_SetUserBanState_Request request);

    /* CChatRoom_RevokeInvite_Response */
    AsyncJobSingle<ServiceMethodResponse> RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request);

    /* CChatRoom_CreateRole_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateRole(CChatRoom_CreateRole_Request request);

    /* CChatRoom_GetRoles_Response */
    AsyncJobSingle<ServiceMethodResponse> GetRoles(CChatRoom_GetRoles_Request request);

    /* CChatRoom_RenameRole_Response */
    AsyncJobSingle<ServiceMethodResponse> RenameRole(CChatRoom_RenameRole_Request request);

    /* CChatRoom_ReorderRole_Response */
    AsyncJobSingle<ServiceMethodResponse> ReorderRole(CChatRoom_ReorderRole_Request request);

    /* CChatRoom_DeleteRole_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteRole(CChatRoom_DeleteRole_Request request);

    /* CChatRoom_GetRoleActions_Response */
    AsyncJobSingle<ServiceMethodResponse> GetRoleActions(CChatRoom_GetRoleActions_Request request);

    /* CChatRoom_ReplaceRoleActions_Response */
    AsyncJobSingle<ServiceMethodResponse> ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request);

    /* CChatRoom_AddRoleToUser_Response */
    AsyncJobSingle<ServiceMethodResponse> AddRoleToUser(CChatRoom_AddRoleToUser_Request request);

    /* CChatRoom_GetRolesForUser_Response */
    AsyncJobSingle<ServiceMethodResponse> GetRolesForUser(CChatRoom_GetRolesForUser_Request request);

    /* CChatRoom_DeleteRoleFromUser_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request);

    /* CChatRoom_JoinChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request);

    /* CChatRoom_InviteFriendToChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request);

    /* CChatRoom_LeaveChatRoomGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request);

    /* CChatRoom_CreateChatRoom_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateChatRoom(CChatRoom_CreateChatRoom_Request request);

    /* CChatRoom_DeleteChatRoom_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request);

    /* CChatRoom_RenameChatRoom_Response */
    AsyncJobSingle<ServiceMethodResponse> RenameChatRoom(CChatRoom_RenameChatRoom_Request request);

    /* CChatRoom_ReorderChatRoom_Response */
    AsyncJobSingle<ServiceMethodResponse> ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request);

    /* CChatRoom_SendChatMessage_Response */
    AsyncJobSingle<ServiceMethodResponse> SendChatMessage(CChatRoom_SendChatMessage_Request request);

    /* CChatRoom_JoinVoiceChat_Response */
    AsyncJobSingle<ServiceMethodResponse> JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request);

    /* CChatRoom_LeaveVoiceChat_Response */
    AsyncJobSingle<ServiceMethodResponse> LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request);

    /* CChatRoom_GetMessageHistory_Response */
    AsyncJobSingle<ServiceMethodResponse> GetMessageHistory(CChatRoom_GetMessageHistory_Request request);

    /* CChatRoom_GetMyChatRoomGroups_Response */
    AsyncJobSingle<ServiceMethodResponse> GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request);

    /* CChatRoom_GetChatRoomGroupState_Response */
    AsyncJobSingle<ServiceMethodResponse> GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request);

    /* CChatRoom_GetChatRoomGroupSummary_Response */
    AsyncJobSingle<ServiceMethodResponse> GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request);

    /* CChatRoom_SetAppChatRoomGroupForceActive_Response */
    AsyncJobSingle<ServiceMethodResponse> SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request);

    /* NoResponse */
    void SetAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request);

    /* NoResponse */
    void AckChatMessage(CChatRoom_AckChatMessage_Notification request);

    /* CChatRoom_CreateInviteLink_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateInviteLink(CChatRoom_CreateInviteLink_Request request);

    /* CChatRoom_GetInviteLinkInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request);

    /* CChatRoom_GetInviteInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetInviteInfo(CChatRoom_GetInviteInfo_Request request);

    /* CChatRoom_GetInviteLinksForGroup_Response */
    AsyncJobSingle<ServiceMethodResponse> GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request);

    /* CChatRoom_GetBanList_Response */
    AsyncJobSingle<ServiceMethodResponse> GetBanList(CChatRoom_GetBanList_Request request);

    /*CChatRoom_GetInviteList_Response */
    AsyncJobSingle<ServiceMethodResponse> GetInviteList(CChatRoom_GetInviteList_Request request);

    /* CChatRoom_DeleteInviteLink_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request);

    /* CChatRoom_SetSessionActiveChatRoomGroups_Response */
    AsyncJobSingle<ServiceMethodResponse> SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request);

    /* CChatRoom_SetUserChatGroupPreferences_Response */
    AsyncJobSingle<ServiceMethodResponse> SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request);

    /* CChatRoom_DeleteChatMessages_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request);

    /* NoResponse */
    void UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request);

    /* CChatRoom_SearchMembers_Response */
    AsyncJobSingle<ServiceMethodResponse> SearchMembers(CChatRoom_SearchMembers_Request request);

    /* CChatRoom_UpdateMessageReaction_Response */
    AsyncJobSingle<ServiceMethodResponse> UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request);

    /* CChatRoom_GetMessageReactionReactors_Response */
    AsyncJobSingle<ServiceMethodResponse> GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request);
}
