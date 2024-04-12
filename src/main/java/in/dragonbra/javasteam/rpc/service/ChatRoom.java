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
    public AsyncJobSingle<ServiceMethodResponse> createChatRoomGroup(CChatRoom_CreateChatRoomGroup_Request request) {
        return sendMessage(request, "CreateChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> saveChatRoomGroup(CChatRoom_SaveChatRoomGroup_Request request) {
        return sendMessage(request, "SaveChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> renameChatRoomGroup(CChatRoom_RenameChatRoomGroup_Request request) {
        return sendMessage(request, "RenameChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setChatRoomGroupTagline(CChatRoom_SetChatRoomGroupTagline_Request request) {
        return sendMessage(request, "SetChatRoomGroupTagline");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setChatRoomGroupAvatar(CChatRoom_SetChatRoomGroupAvatar_Request request) {
        return sendMessage(request, "SetChatRoomGroupAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setChatRoomGroupWatchingBroadcast(CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request) {
        return sendMessage(request, "SetChatRoomGroupWatchingBroadcast");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> joinMiniGameForChatRoomGroup(CChatRoom_JoinMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "JoinMiniGameForChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> endMiniGameForChatRoomGroup(CChatRoom_EndMiniGameForChatRoomGroup_Request request) {
        return sendMessage(request, "EndMiniGameForChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> muteUserInGroup(CChatRoom_MuteUser_Request request) {
        return sendMessage(request, "MuteUserInGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> kickUserFromGroup(CChatRoom_KickUser_Request request) {
        return sendMessage(request, "KickUserFromGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setUserBanState(CChatRoom_SetUserBanState_Request request) {
        return sendMessage(request, "SetUserBanState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> revokeInviteToGroup(CChatRoom_RevokeInvite_Request request) {
        return sendMessage(request, "RevokeInviteToGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> createRole(CChatRoom_CreateRole_Request request) {
        return sendMessage(request, "CreateRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getRoles(CChatRoom_GetRoles_Request request) {
        return sendMessage(request, "GetRoles");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> renameRole(CChatRoom_RenameRole_Request request) {
        return sendMessage(request, "RenameRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> reorderRole(CChatRoom_ReorderRole_Request request) {
        return sendMessage(request, "ReorderRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deleteRole(CChatRoom_DeleteRole_Request request) {
        return sendMessage(request, "DeleteRole");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getRoleActions(CChatRoom_GetRoleActions_Request request) {
        return sendMessage(request, "GetRoleActions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> replaceRoleActions(CChatRoom_ReplaceRoleActions_Request request) {
        return sendMessage(request, "ReplaceRoleActions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> addRoleToUser(CChatRoom_AddRoleToUser_Request request) {
        return sendMessage(request, "AddRoleToUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getRolesForUser(CChatRoom_GetRolesForUser_Request request) {
        return sendMessage(request, "GetRolesForUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deleteRoleFromUser(CChatRoom_DeleteRoleFromUser_Request request) {
        return sendMessage(request, "DeleteRoleFromUser");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> joinChatRoomGroup(CChatRoom_JoinChatRoomGroup_Request request) {
        return sendMessage(request, "JoinChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> inviteFriendToChatRoomGroup(CChatRoom_InviteFriendToChatRoomGroup_Request request) {
        return sendMessage(request, "InviteFriendToChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> leaveChatRoomGroup(CChatRoom_LeaveChatRoomGroup_Request request) {
        return sendMessage(request, "LeaveChatRoomGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> createChatRoom(CChatRoom_CreateChatRoom_Request request) {
        return sendMessage(request, "CreateChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deleteChatRoom(CChatRoom_DeleteChatRoom_Request request) {
        return sendMessage(request, "DeleteChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> renameChatRoom(CChatRoom_RenameChatRoom_Request request) {
        return sendMessage(request, "RenameChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> reorderChatRoom(CChatRoom_ReorderChatRoom_Request request) {
        return sendMessage(request, "ReorderChatRoom");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> sendChatMessage(CChatRoom_SendChatMessage_Request request) {
        return sendMessage(request, "SendChatMessage");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> joinVoiceChat(CChatRoom_JoinVoiceChat_Request request) {
        return sendMessage(request, "JoinVoiceChat");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> leaveVoiceChat(CChatRoom_LeaveVoiceChat_Request request) {
        return sendMessage(request, "LeaveVoiceChat");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getMessageHistory(CChatRoom_GetMessageHistory_Request request) {
        return sendMessage(request, "GetMessageHistory");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getMyChatRoomGroups(CChatRoom_GetMyChatRoomGroups_Request request) {
        return sendMessage(request, "GetMyChatRoomGroups");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getChatRoomGroupState(CChatRoom_GetChatRoomGroupState_Request request) {
        return sendMessage(request, "GetChatRoomGroupState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getChatRoomGroupSummary(CChatRoom_GetChatRoomGroupSummary_Request request) {
        return sendMessage(request, "GetChatRoomGroupSummary");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setAppChatRoomGroupForceActive(CChatRoom_SetAppChatRoomGroupForceActive_Request request) {
        return sendMessage(request, "SetAppChatRoomGroupForceActive");
    }

    @Override
    public void setAppChatRoomGroupStopForceActive(CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request) {
        sendNotification(request, "SetAppChatRoomGroupStopForceActive");
    }

    @Override
    public void ackChatMessage(CChatRoom_AckChatMessage_Notification request) {
        sendNotification(request, "AckChatMessage");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> createInviteLink(CChatRoom_CreateInviteLink_Request request) {
        return sendMessage(request, "CreateInviteLink");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getInviteLinkInfo(CChatRoom_GetInviteLinkInfo_Request request) {
        return sendMessage(request, "GetInviteLinkInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getInviteInfo(CChatRoom_GetInviteInfo_Request request) {
        return sendMessage(request, "GetInviteInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getInviteLinksForGroup(CChatRoom_GetInviteLinksForGroup_Request request) {
        return sendMessage(request, "GetInviteLinksForGroup");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getBanList(CChatRoom_GetBanList_Request request) {
        return sendMessage(request, "GetBanList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getInviteList(CChatRoom_GetInviteList_Request request) {
        return sendMessage(request, "GetInviteList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deleteInviteLink(CChatRoom_DeleteInviteLink_Request request) {
        return sendMessage(request, "DeleteInviteLink");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setSessionActiveChatRoomGroups(CChatRoom_SetSessionActiveChatRoomGroups_Request request) {
        return sendMessage(request, "SetSessionActiveChatRoomGroups");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setUserChatGroupPreferences(CChatRoom_SetUserChatGroupPreferences_Request request) {
        return sendMessage(request, "SetUserChatGroupPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deleteChatMessages(CChatRoom_DeleteChatMessages_Request request) {
        return sendMessage(request, "DeleteChatMessages");
    }

    @Override
    public void updateMemberListView(CChatRoom_UpdateMemberListView_Notification request) {
        sendNotification(request, "UpdateMemberListView");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> searchMembers(CChatRoom_SearchMembers_Request request) {
        return sendMessage(request, "SearchMembers");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateMessageReaction(CChatRoom_UpdateMessageReaction_Request request) {
        return sendMessage(request, "UpdateMessageReaction");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getMessageReactionReactors(CChatRoom_GetMessageReactionReactors_Request request) {
        return sendMessage(request, "GetMessageReactionReactors");
    }
}
