package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient;
import in.dragonbra.javasteam.rpc.IChatRoom;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement
@SuppressWarnings("unused")
public class ChatRoom extends UnifiedService implements IChatRoom {

    public ChatRoom(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID CreateChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_CreateChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID SaveChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_SaveChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID RenameChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_RenameChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID SetChatRoomGroupTagline(SteammessagesChatSteamclient.CChatRoom_SetChatRoomGroupTagline_Request request) {
        return null;
    }

    @Override
    public JobID SetChatRoomGroupAvatar(SteammessagesChatSteamclient.CChatRoom_SetChatRoomGroupAvatar_Request request) {
        return null;
    }

    @Override
    public JobID SetChatRoomGroupWatchingBroadcast(SteammessagesChatSteamclient.CChatRoom_SetChatRoomGroupWatchingBroadcast_Request request) {
        return null;
    }

    @Override
    public JobID JoinMiniGameForChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_JoinMiniGameForChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID EndMiniGameForChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_EndMiniGameForChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID MuteUserInGroup(SteammessagesChatSteamclient.CChatRoom_MuteUser_Request request) {
        return null;
    }

    @Override
    public JobID KickUserFromGroup(SteammessagesChatSteamclient.CChatRoom_KickUser_Request request) {
        return null;
    }

    @Override
    public JobID SetUserBanState(SteammessagesChatSteamclient.CChatRoom_SetUserBanState_Request request) {
        return null;
    }

    @Override
    public JobID RevokeInviteToGroup(SteammessagesChatSteamclient.CChatRoom_RevokeInvite_Request request) {
        return null;
    }

    @Override
    public JobID CreateRole(SteammessagesChatSteamclient.CChatRoom_CreateRole_Request request) {
        return null;
    }

    @Override
    public JobID GetRoles(SteammessagesChatSteamclient.CChatRoom_GetRoles_Request request) {
        return null;
    }

    @Override
    public JobID RenameRole(SteammessagesChatSteamclient.CChatRoom_RenameRole_Request request) {
        return null;
    }

    @Override
    public JobID ReorderRole(SteammessagesChatSteamclient.CChatRoom_ReorderRole_Request request) {
        return null;
    }

    @Override
    public JobID DeleteRole(SteammessagesChatSteamclient.CChatRoom_DeleteRole_Request request) {
        return null;
    }

    @Override
    public JobID GetRoleActions(SteammessagesChatSteamclient.CChatRoom_GetRoleActions_Request request) {
        return null;
    }

    @Override
    public JobID ReplaceRoleActions(SteammessagesChatSteamclient.CChatRoom_ReplaceRoleActions_Request request) {
        return null;
    }

    @Override
    public JobID AddRoleToUser(SteammessagesChatSteamclient.CChatRoom_AddRoleToUser_Request request) {
        return null;
    }

    @Override
    public JobID GetRolesForUser(SteammessagesChatSteamclient.CChatRoom_GetRolesForUser_Request request) {
        return null;
    }

    @Override
    public JobID DeleteRoleFromUser(SteammessagesChatSteamclient.CChatRoom_DeleteRoleFromUser_Request request) {
        return null;
    }

    @Override
    public JobID JoinChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_JoinChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID InviteFriendToChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_InviteFriendToChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID LeaveChatRoomGroup(SteammessagesChatSteamclient.CChatRoom_LeaveChatRoomGroup_Request request) {
        return null;
    }

    @Override
    public JobID CreateChatRoom(SteammessagesChatSteamclient.CChatRoom_CreateChatRoom_Request request) {
        return null;
    }

    @Override
    public JobID DeleteChatRoom(SteammessagesChatSteamclient.CChatRoom_DeleteChatRoom_Request request) {
        return null;
    }

    @Override
    public JobID RenameChatRoom(SteammessagesChatSteamclient.CChatRoom_RenameChatRoom_Request request) {
        return null;
    }

    @Override
    public JobID ReorderChatRoom(SteammessagesChatSteamclient.CChatRoom_ReorderChatRoom_Request request) {
        return null;
    }

    @Override
    public JobID SendChatMessage(SteammessagesChatSteamclient.CChatRoom_SendChatMessage_Request request) {
        return null;
    }

    @Override
    public JobID JoinVoiceChat(SteammessagesChatSteamclient.CChatRoom_JoinVoiceChat_Request request) {
        return null;
    }

    @Override
    public JobID LeaveVoiceChat(SteammessagesChatSteamclient.CChatRoom_LeaveVoiceChat_Request request) {
        return null;
    }

    @Override
    public JobID GetMessageHistory(SteammessagesChatSteamclient.CChatRoom_GetMessageHistory_Request request) {
        return null;
    }

    @Override
    public JobID GetMyChatRoomGroups(SteammessagesChatSteamclient.CChatRoom_GetMyChatRoomGroups_Request request) {
        return null;
    }

    @Override
    public JobID GetChatRoomGroupState(SteammessagesChatSteamclient.CChatRoom_GetChatRoomGroupState_Request request) {
        return null;
    }

    @Override
    public JobID GetChatRoomGroupSummary(SteammessagesChatSteamclient.CChatRoom_GetChatRoomGroupSummary_Request request) {
        return null;
    }

    @Override
    public JobID SetAppChatRoomGroupForceActive(SteammessagesChatSteamclient.CChatRoom_SetAppChatRoomGroupForceActive_Request request) {
        return null;
    }

    @Override
    public void SetAppChatRoomGroupStopForceActive(SteammessagesChatSteamclient.CChatRoom_SetAppChatRoomGroupStopForceActive_Notification request) {

    }

    @Override
    public void AckChatMessage(SteammessagesChatSteamclient.CChatRoom_AckChatMessage_Notification request) {

    }

    @Override
    public JobID CreateInviteLink(SteammessagesChatSteamclient.CChatRoom_CreateInviteLink_Request request) {
        return null;
    }

    @Override
    public JobID GetInviteLinkInfo(SteammessagesChatSteamclient.CChatRoom_GetInviteLinkInfo_Request request) {
        return null;
    }

    @Override
    public JobID GetInviteInfo(SteammessagesChatSteamclient.CChatRoom_GetInviteInfo_Request request) {
        return null;
    }

    @Override
    public JobID GetInviteLinksForGroup(SteammessagesChatSteamclient.CChatRoom_GetInviteLinksForGroup_Request request) {
        return null;
    }

    @Override
    public JobID GetBanList(SteammessagesChatSteamclient.CChatRoom_GetBanList_Request request) {
        return null;
    }

    @Override
    public JobID GetInviteList(SteammessagesChatSteamclient.CChatRoom_GetInviteList_Request request) {
        return null;
    }

    @Override
    public JobID DeleteInviteLink(SteammessagesChatSteamclient.CChatRoom_DeleteInviteLink_Request request) {
        return null;
    }

    @Override
    public JobID SetSessionActiveChatRoomGroups(SteammessagesChatSteamclient.CChatRoom_SetSessionActiveChatRoomGroups_Request request) {
        return null;
    }

    @Override
    public JobID SetUserChatGroupPreferences(SteammessagesChatSteamclient.CChatRoom_SetUserChatGroupPreferences_Request request) {
        return null;
    }

    @Override
    public JobID DeleteChatMessages(SteammessagesChatSteamclient.CChatRoom_DeleteChatMessages_Request request) {
        return null;
    }

    @Override
    public void UpdateMemberListView(SteammessagesChatSteamclient.CChatRoom_UpdateMemberListView_Notification request) {

    }

    @Override
    public JobID SearchMembers(SteammessagesChatSteamclient.CChatRoom_SearchMembers_Request request) {
        return null;
    }

    @Override
    public JobID UpdateMessageReaction(SteammessagesChatSteamclient.CChatRoom_UpdateMessageReaction_Request request) {
        return null;
    }

    @Override
    public JobID GetMessageReactionReactors(SteammessagesChatSteamclient.CChatRoom_GetMessageReactionReactors_Request request) {
        return null;
    }
}
