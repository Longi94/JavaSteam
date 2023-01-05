package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.rpc.IChatRoom;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ChatRoom extends UnifiedService implements IChatRoom {

    public ChatRoom(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID CreateChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_CreateChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_SaveChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_RenameChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request) {
        return sendMessage(CChatRoom_SetChatRoomGroupTagline_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request) {
        return sendMessage(CChatRoom_SetChatRoomGroupAvatar_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request) {
        return sendMessage(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request.class, request.toBuilder());
    }

    @Override
    public JobID JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_JoinMiniGameForChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_EndMiniGameForChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID MuteUserInGroup(CChatRoom_MuteUser_Request request) {
        return sendMessage(CChatRoom_MuteUser_Request.class, request.toBuilder());
    }

    @Override
    public JobID KickUserFromGroup(CChatRoom_KickUser_Request request) {
        return sendMessage(CChatRoom_KickUser_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetUserBanState(CChatRoom_SetUserBanState_Request request) {
        return sendMessage(CChatRoom_SetUserBanState_Request.class, request.toBuilder());
    }

    @Override
    public JobID RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request) {
        return sendMessage(CChatRoom_RevokeInvite_Request.class, request.toBuilder());
    }

    @Override
    public JobID CreateRole(CChatRoom_CreateRole_Request request) {
        return sendMessage(CChatRoom_CreateRole_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetRoles(CChatRoom_GetRoles_Request request) {
        return sendMessage(CChatRoom_GetRoles_Request.class, request.toBuilder());
    }

    @Override
    public JobID RenameRole(CChatRoom_RenameRole_Request request) {
        return sendMessage(CChatRoom_RenameRole_Request.class, request.toBuilder());
    }

    @Override
    public JobID ReorderRole(CChatRoom_ReorderRole_Request request) {
        return sendMessage(CChatRoom_ReorderRole_Request.class, request.toBuilder());
    }

    @Override
    public JobID DeleteRole(CChatRoom_DeleteRole_Request request) {
        return sendMessage(CChatRoom_DeleteRole_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetRoleActions(CChatRoom_GetRoleActions_Request request) {
        return sendMessage(CChatRoom_GetRoleActions_Request.class, request.toBuilder());
    }

    @Override
    public JobID ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request) {
        return sendMessage(CChatRoom_ReplaceRoleActions_Request.class, request.toBuilder());
    }

    @Override
    public JobID AddRoleToUser(CChatRoom_AddRoleToUser_Request request) {
        return sendMessage(CChatRoom_AddRoleToUser_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetRolesForUser(CChatRoom_GetRolesForUser_Request request) {
        return sendMessage(CChatRoom_GetRolesForUser_Request.class, request.toBuilder());
    }

    @Override
    public JobID DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request) {
        return sendMessage(CChatRoom_DeleteRoleFromUser_Request.class, request.toBuilder());
    }

    @Override
    public JobID JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_JoinChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_InviteFriendToChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request) {
        return sendMessage(CChatRoom_LeaveChatRoomGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID CreateChatRoom(CChatRoom_CreateChatRoom_Request request) {
        return sendMessage(CChatRoom_CreateChatRoom_Request.class, request.toBuilder());
    }

    @Override
    public JobID DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request) {
        return sendMessage(CChatRoom_DeleteChatRoom_Request.class, request.toBuilder());
    }

    @Override
    public JobID RenameChatRoom(CChatRoom_RenameChatRoom_Request request) {
        return sendMessage(CChatRoom_RenameChatRoom_Request.class, request.toBuilder());
    }

    @Override
    public JobID ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request) {
        return sendMessage(CChatRoom_ReorderChatRoom_Request.class, request.toBuilder());
    }

    @Override
    public JobID SendChatMessage(CChatRoom_SendChatMessage_Request request) {
        return sendMessage(CChatRoom_SendChatMessage_Request.class, request.toBuilder());
    }

    @Override
    public JobID JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request) {
        return sendMessage(CChatRoom_JoinVoiceChat_Request.class, request.toBuilder());
    }

    @Override
    public JobID LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request) {
        return sendMessage(CChatRoom_LeaveVoiceChat_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetMessageHistory(CChatRoom_GetMessageHistory_Request request) {
        return sendMessage(CChatRoom_GetMessageHistory_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request) {
        return sendMessage(CChatRoom_GetMyChatRoomGroups_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request) {
        return sendMessage(CChatRoom_GetChatRoomGroupState_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request) {
        return sendMessage(CChatRoom_GetChatRoomGroupSummary_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request) {
        return sendMessage(CChatRoom_SetAppChatRoomGroupForceActive_Request.class, request.toBuilder());
    }

    @Override
    public void SetAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request) {
        sendNotification(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification.class, request.toBuilder());
    }

    @Override
    public void AckChatMessage(CChatRoom_AckChatMessage_Notification request) {
        sendNotification(CChatRoom_AckChatMessage_Notification.class, request.toBuilder());
    }

    @Override
    public JobID CreateInviteLink(CChatRoom_CreateInviteLink_Request request) {
        return sendMessage(CChatRoom_CreateInviteLink_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request) {
        return sendMessage(CChatRoom_GetInviteLinkInfo_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetInviteInfo(CChatRoom_GetInviteInfo_Request request) {
        return sendMessage(CChatRoom_GetInviteInfo_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request) {
        return sendMessage(CChatRoom_GetInviteLinksForGroup_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetBanList(CChatRoom_GetBanList_Request request) {
        return sendMessage(CChatRoom_GetBanList_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetInviteList(CChatRoom_GetInviteList_Request request) {
        return sendMessage(CChatRoom_GetInviteList_Request.class, request.toBuilder());
    }

    @Override
    public JobID DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request) {
        return sendMessage(CChatRoom_DeleteInviteLink_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request) {
        return sendMessage(CChatRoom_SetSessionActiveChatRoomGroups_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request) {
        return sendMessage(CChatRoom_SetUserChatGroupPreferences_Request.class, request.toBuilder());
    }

    @Override
    public JobID DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request) {
        return sendMessage(CChatRoom_DeleteChatMessages_Request.class, request.toBuilder());
    }

    @Override
    public void UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request) {
        sendNotification(CChatRoom_UpdateMemberListView_Notification.class, request.toBuilder());
    }

    @Override
    public JobID SearchMembers(CChatRoom_SearchMembers_Request request) {
        return sendMessage(CChatRoom_SearchMembers_Request.class, request.toBuilder());
    }

    @Override
    public JobID UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request) {
        return sendMessage(CChatRoom_UpdateMessageReaction_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request) {
        return sendMessage(CChatRoom_GetMessageReactionReactors_Request.class, request.toBuilder());
    }
}
