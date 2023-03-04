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
        return sendMessage(request, "CreateChatRoomGroup");
    }

    @Override
    public JobID SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request) {
        return sendMessage(request, "SaveChatRoomGroup");
    }

    @Override
    public JobID RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request) {
        return sendMessage(request, "RenameChatRoomGroup");
    }

    @Override
    public JobID SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request) {
        return sendMessage(request, "SetChatRoomGroupTagline");
    }

    @Override
    public JobID SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request) {
        return sendMessage(request, "SetChatRoomGroupAvatar");
    }

    @Override
    public JobID SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request) {
        return sendMessage(request, "SetChatRoomGroupWatchingBroadcast");
    }

    @Override
    public JobID JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "JoinMiniGameForChatRoomGroup");
    }

    @Override
    public JobID EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "EndMiniGameForChatRoomGroup");
    }

    @Override
    public JobID MuteUserInGroup(CChatRoom_MuteUser_Request request) {
        return sendMessage(request, "MuteUserInGroup");
    }

    @Override
    public JobID KickUserFromGroup(CChatRoom_KickUser_Request request) {
        return sendMessage(request, "KickUserFromGroup");
    }

    @Override
    public JobID SetUserBanState(CChatRoom_SetUserBanState_Request request) {
        return sendMessage(request, "SetUserBanState");
    }

    @Override
    public JobID RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request) {
        return sendMessage(request, "RevokeInviteToGroup");
    }

    @Override
    public JobID CreateRole(CChatRoom_CreateRole_Request request) {
        return sendMessage(request, "CreateRole");
    }

    @Override
    public JobID GetRoles(CChatRoom_GetRoles_Request request) {
        return sendMessage(request, "GetRoles");
    }

    @Override
    public JobID RenameRole(CChatRoom_RenameRole_Request request) {
        return sendMessage(request, "RenameRole");
    }

    @Override
    public JobID ReorderRole(CChatRoom_ReorderRole_Request request) {
        return sendMessage(request, "ReorderRole");
    }

    @Override
    public JobID DeleteRole(CChatRoom_DeleteRole_Request request) {
        return sendMessage(request, "DeleteRole");
    }

    @Override
    public JobID GetRoleActions(CChatRoom_GetRoleActions_Request request) {
        return sendMessage(request, "GetRoleActions");
    }

    @Override
    public JobID ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request) {
        return sendMessage(request, "ReplaceRoleActions");
    }

    @Override
    public JobID AddRoleToUser(CChatRoom_AddRoleToUser_Request request) {
        return sendMessage(request, "AddRoleToUser");
    }

    @Override
    public JobID GetRolesForUser(CChatRoom_GetRolesForUser_Request request) {
        return sendMessage(request, "GetRolesForUser");
    }

    @Override
    public JobID DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request) {
        return sendMessage(request, "DeleteRoleFromUser");
    }

    @Override
    public JobID JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request) {
        return sendMessage(request, "JoinChatRoomGroup");
    }

    @Override
    public JobID InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request) {
        return sendMessage(request, "InviteFriendToChatRoomGroup");
    }

    @Override
    public JobID LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request) {
        return sendMessage(request, "LeaveChatRoomGroup");
    }

    @Override
    public JobID CreateChatRoom(CChatRoom_CreateChatRoom_Request request) {
        return sendMessage(request, "CreateChatRoom");
    }

    @Override
    public JobID DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request) {
        return sendMessage(request, "DeleteChatRoom");
    }

    @Override
    public JobID RenameChatRoom(CChatRoom_RenameChatRoom_Request request) {
        return sendMessage(request, "RenameChatRoom");
    }

    @Override
    public JobID ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request) {
        return sendMessage(request, "ReorderChatRoom");
    }

    @Override
    public JobID SendChatMessage(CChatRoom_SendChatMessage_Request request) {
        return sendMessage(request, "SendChatMessage");
    }

    @Override
    public JobID JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request) {
        return sendMessage(request, "JoinVoiceChat");
    }

    @Override
    public JobID LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request) {
        return sendMessage(request, "LeaveVoiceChat");
    }

    @Override
    public JobID GetMessageHistory(CChatRoom_GetMessageHistory_Request request) {
        return sendMessage(request, "GetMessageHistory");
    }

    @Override
    public JobID GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request) {
        return sendMessage(request, "GetMyChatRoomGroups");
    }

    @Override
    public JobID GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request) {
        return sendMessage(request, "GetChatRoomGroupState");
    }

    @Override
    public JobID GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request) {
        return sendMessage(request, "GetChatRoomGroupSummary");
    }

    @Override
    public JobID SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request) {
        return sendMessage(request, "SetAppChatRoomGroupForceActive");
    }

    @Override
    public void SetAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request) {
        sendNotification(request, "SetAppChatRoomGroupStopForceActive");
    }

    @Override
    public void AckChatMessage(CChatRoom_AckChatMessage_Notification request) {
        sendNotification(request, "AckChatMessage");
    }

    @Override
    public JobID CreateInviteLink(CChatRoom_CreateInviteLink_Request request) {
        return sendMessage(request, "CreateInviteLink");
    }

    @Override
    public JobID GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request) {
        return sendMessage(request, "GetInviteLinkInfo");
    }

    @Override
    public JobID GetInviteInfo(CChatRoom_GetInviteInfo_Request request) {
        return sendMessage(request, "GetInviteInfo");
    }

    @Override
    public JobID GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request) {
        return sendMessage(request, "GetInviteLinksForGroup");
    }

    @Override
    public JobID GetBanList(CChatRoom_GetBanList_Request request) {
        return sendMessage(request, "GetBanList");
    }

    @Override
    public JobID GetInviteList(CChatRoom_GetInviteList_Request request) {
        return sendMessage(request, "GetInviteList");
    }

    @Override
    public JobID DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request) {
        return sendMessage(request, "DeleteInviteLink");
    }

    @Override
    public JobID SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request) {
        return sendMessage(request, "SetSessionActiveChatRoomGroups");
    }

    @Override
    public JobID SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request) {
        return sendMessage(request, "SetUserChatGroupPreferences");
    }

    @Override
    public JobID DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request) {
        return sendMessage(request, "DeleteChatMessages");
    }

    @Override
    public void UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request) {
        sendNotification(request, "UpdateMemberListView");
    }

    @Override
    public JobID SearchMembers(CChatRoom_SearchMembers_Request request) {
        return sendMessage(request, "SearchMembers");
    }

    @Override
    public JobID UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request) {
        return sendMessage(request, "UpdateMessageReaction");
    }

    @Override
    public JobID GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request) {
        return sendMessage(request, "GetMessageReactionReactors");
    }
}
