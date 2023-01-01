package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IChatRoom {
    CChatRoom_CreateChatRoomGroup_Response CreateChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request);

    CChatRoom_SaveChatRoomGroup_Response SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request);

    CChatRoom_RenameChatRoomGroup_Response RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request);

    CChatRoom_SetChatRoomGroupTagline_Response SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request);

    CChatRoom_SetChatRoomGroupAvatar_Response SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request);

    CChatRoom_SetChatRoomGroupWatchingBroadcast_Response SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request);

    CChatRoom_JoinMiniGameForChatRoomGroup_Response JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request);

    CChatRoom_EndMiniGameForChatRoomGroup_Response EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request);

    CChatRoom_MuteUser_Response MuteUserInGroup(CChatRoom_MuteUser_Request request);

    CChatRoom_KickUser_Response KickUserFromGroup(CChatRoom_KickUser_Request request);

    CChatRoom_SetUserBanState_Response SetUserBanState(CChatRoom_SetUserBanState_Request request);

    CChatRoom_RevokeInvite_Response RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request);

    CChatRoom_CreateRole_Response CreateRole(CChatRoom_CreateRole_Request request);

    CChatRoom_GetRoles_Response GetRoles(CChatRoom_GetRoles_Request request);

    CChatRoom_RenameRole_Response RenameRole(CChatRoom_RenameRole_Request request);

    CChatRoom_ReorderRole_Response ReorderRole(CChatRoom_ReorderRole_Request request);

    CChatRoom_DeleteRole_Response DeleteRole(CChatRoom_DeleteRole_Request request);

    CChatRoom_GetRoleActions_Response GetRoleActions(CChatRoom_GetRoleActions_Request request);

    CChatRoom_ReplaceRoleActions_Response ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request);

    CChatRoom_AddRoleToUser_Response AddRoleToUser(CChatRoom_AddRoleToUser_Request request);

    CChatRoom_GetRolesForUser_Response GetRolesForUser(CChatRoom_GetRolesForUser_Request request);

    CChatRoom_DeleteRoleFromUser_Response DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request);

    CChatRoom_JoinChatRoomGroup_Response JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request);

    CChatRoom_InviteFriendToChatRoomGroup_Response InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request);

    CChatRoom_LeaveChatRoomGroup_Response LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request);

    CChatRoom_CreateChatRoom_Response CreateChatRoom(CChatRoom_CreateChatRoom_Request request);

    CChatRoom_DeleteChatRoom_Response DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request);

    CChatRoom_RenameChatRoom_Response RenameChatRoom(CChatRoom_RenameChatRoom_Request request);

    CChatRoom_ReorderChatRoom_Response ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request);

    CChatRoom_SendChatMessage_Response SendChatMessage(CChatRoom_SendChatMessage_Request request);

    CChatRoom_JoinVoiceChat_Response JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request);

    CChatRoom_LeaveVoiceChat_Response LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request);

    CChatRoom_GetMessageHistory_Response GetMessageHistory(CChatRoom_GetMessageHistory_Request request);

    CChatRoom_GetMyChatRoomGroups_Response GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request);

    CChatRoom_GetChatRoomGroupState_Response GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request);

    CChatRoom_GetChatRoomGroupSummary_Response GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request);

    CChatRoom_SetAppChatRoomGroupForceActive_Response SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request);

    NoResponse SetAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request);

    NoResponse AckChatMessage(CChatRoom_AckChatMessage_Notification request);

    CChatRoom_CreateInviteLink_Response CreateInviteLink(CChatRoom_CreateInviteLink_Request request);

    CChatRoom_GetInviteLinkInfo_Response GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request);

    CChatRoom_GetInviteInfo_Response GetInviteInfo(CChatRoom_GetInviteInfo_Request request);

    CChatRoom_GetInviteLinksForGroup_Response GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request);

    CChatRoom_GetBanList_Response GetBanList(CChatRoom_GetBanList_Request request);

    CChatRoom_GetInviteList_Response GetInviteList(CChatRoom_GetInviteList_Request request);

    CChatRoom_DeleteInviteLink_Response DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request);

    CChatRoom_SetSessionActiveChatRoomGroups_Response SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request);

    CChatRoom_SetUserChatGroupPreferences_Response SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request);

    CChatRoom_DeleteChatMessages_Response DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request);

    NoResponse UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request);

    CChatRoom_SearchMembers_Response SearchMembers(CChatRoom_SearchMembers_Request request);

    CChatRoom_UpdateMessageReaction_Response UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request);

    CChatRoom_GetMessageReactionReactors_Response GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request);
}
