package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IChatRoom;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> CreateChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request) {
        return sendMessage(request, "CreateChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SaveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request) {
        return sendMessage(request, "SaveChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RenameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request) {
        return sendMessage(request, "RenameChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request) {
        return sendMessage(request, "SetChatRoomGroupTagline");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request) {
        return sendMessage(request, "SetChatRoomGroupAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request) {
        return sendMessage(request, "SetChatRoomGroupWatchingBroadcast");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> JoinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "JoinMiniGameForChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> EndMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "EndMiniGameForChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> MuteUserInGroup(CChatRoom_MuteUser_Request request) {
        return sendMessage(request, "MuteUserInGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> KickUserFromGroup(CChatRoom_KickUser_Request request) {
        return sendMessage(request, "KickUserFromGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetUserBanState(CChatRoom_SetUserBanState_Request request) {
        return sendMessage(request, "SetUserBanState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RevokeInviteToGroup(CChatRoom_RevokeInvite_Request request) {
        return sendMessage(request, "RevokeInviteToGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> CreateRole(CChatRoom_CreateRole_Request request) {
        return sendMessage(request, "CreateRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetRoles(CChatRoom_GetRoles_Request request) {
        return sendMessage(request, "GetRoles");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RenameRole(CChatRoom_RenameRole_Request request) {
        return sendMessage(request, "RenameRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ReorderRole(CChatRoom_ReorderRole_Request request) {
        return sendMessage(request, "ReorderRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteRole(CChatRoom_DeleteRole_Request request) {
        return sendMessage(request, "DeleteRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetRoleActions(CChatRoom_GetRoleActions_Request request) {
        return sendMessage(request, "GetRoleActions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ReplaceRoleActions(CChatRoom_ReplaceRoleActions_Request request) {
        return sendMessage(request, "ReplaceRoleActions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AddRoleToUser(CChatRoom_AddRoleToUser_Request request) {
        return sendMessage(request, "AddRoleToUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetRolesForUser(CChatRoom_GetRolesForUser_Request request) {
        return sendMessage(request, "GetRolesForUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request) {
        return sendMessage(request, "DeleteRoleFromUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> JoinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request) {
        return sendMessage(request, "JoinChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> InviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request) {
        return sendMessage(request, "InviteFriendToChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> LeaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request) {
        return sendMessage(request, "LeaveChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> CreateChatRoom(CChatRoom_CreateChatRoom_Request request) {
        return sendMessage(request, "CreateChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteChatRoom(CChatRoom_DeleteChatRoom_Request request) {
        return sendMessage(request, "DeleteChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RenameChatRoom(CChatRoom_RenameChatRoom_Request request) {
        return sendMessage(request, "RenameChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ReorderChatRoom(CChatRoom_ReorderChatRoom_Request request) {
        return sendMessage(request, "ReorderChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SendChatMessage(CChatRoom_SendChatMessage_Request request) {
        return sendMessage(request, "SendChatMessage");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> JoinVoiceChat(CChatRoom_JoinVoiceChat_Request request) {
        return sendMessage(request, "JoinVoiceChat");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> LeaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request) {
        return sendMessage(request, "LeaveVoiceChat");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetMessageHistory(CChatRoom_GetMessageHistory_Request request) {
        return sendMessage(request, "GetMessageHistory");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request) {
        return sendMessage(request, "GetMyChatRoomGroups");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request) {
        return sendMessage(request, "GetChatRoomGroupState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request) {
        return sendMessage(request, "GetChatRoomGroupSummary");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request) {
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
    public AsyncJobSingle<ServiceMethodResponse> CreateInviteLink(CChatRoom_CreateInviteLink_Request request) {
        return sendMessage(request, "CreateInviteLink");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request) {
        return sendMessage(request, "GetInviteLinkInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetInviteInfo(CChatRoom_GetInviteInfo_Request request) {
        return sendMessage(request, "GetInviteInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request) {
        return sendMessage(request, "GetInviteLinksForGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetBanList(CChatRoom_GetBanList_Request request) {
        return sendMessage(request, "GetBanList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetInviteList(CChatRoom_GetInviteList_Request request) {
        return sendMessage(request, "GetInviteList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteInviteLink(CChatRoom_DeleteInviteLink_Request request) {
        return sendMessage(request, "DeleteInviteLink");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request) {
        return sendMessage(request, "SetSessionActiveChatRoomGroups");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request) {
        return sendMessage(request, "SetUserChatGroupPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteChatMessages(CChatRoom_DeleteChatMessages_Request request) {
        return sendMessage(request, "DeleteChatMessages");
    }

    @Override
    public void UpdateMemberListView(CChatRoom_UpdateMemberListView_Notification request) {
        sendNotification(request, "UpdateMemberListView");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SearchMembers(CChatRoom_SearchMembers_Request request) {
        return sendMessage(request, "SearchMembers");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateMessageReaction(CChatRoom_UpdateMessageReaction_Request request) {
        return sendMessage(request, "UpdateMessageReaction");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request) {
        return sendMessage(request, "GetMessageReactionReactors");
    }
}
