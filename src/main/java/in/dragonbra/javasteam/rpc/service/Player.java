package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IPlayer;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class Player extends UnifiedService implements IPlayer {

    public Player(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPlayerLinkDetails(CPlayer_GetPlayerLinkDetails_Request request) {
        return sendMessage(request, "GetPlayerLinkDetails");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request) {
        return sendMessage(request, "GetMutualFriendsForIncomingInvites");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetOwnedGames(CPlayer_GetOwnedGames_Request request) {
        return sendMessage(request, "GetOwnedGames");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPlayNext(CPlayer_GetPlayNext_Request request) {
        return sendMessage(request, "GetPlayNext");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request) {
        return sendMessage(request, "GetFriendsGameplayInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request) {
        return sendMessage(request, "GetGameBadgeLevels");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetProfileBackground(CPlayer_GetProfileBackground_Request request) {
        return sendMessage(request, "GetProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetProfileBackground(CPlayer_SetProfileBackground_Request request) {
        return sendMessage(request, "SetProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request) {
        return sendMessage(request, "GetMiniProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request) {
        return sendMessage(request, "SetMiniProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAvatarFrame(CPlayer_GetAvatarFrame_Request request) {
        return sendMessage(request, "GetAvatarFrame");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetAvatarFrame(CPlayer_SetAvatarFrame_Request request) {
        return sendMessage(request, "SetAvatarFrame");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request) {
        return sendMessage(request, "GetAnimatedAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request) {
        return sendMessage(request, "SetAnimatedAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request, "GetSteamDeckKeyboardSkin");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request, "SetSteamDeckKeyboardSkin");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request) {
        return sendMessage(request, "GetProfileItemsOwned");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request) {
        return sendMessage(request, "GetProfileItemsEquipped");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request) {
        return sendMessage(request, "SetEquippedProfileItemFlags");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetEmoticonList(CPlayer_GetEmoticonList_Request request) {
        return sendMessage(request, "GetEmoticonList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAchievementsProgress(CPlayer_GetAchievementsProgress_Request request) {
        return sendMessage(request, "GetAchievementsProgress");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetFavoriteBadge(CPlayer_GetFavoriteBadge_Request request) {
        return sendMessage(request, "GetFavoriteBadge");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetFavoriteBadge(CPlayer_SetFavoriteBadge_Request request) {
        return sendMessage(request, "SetFavoriteBadge");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetProfileCustomization(CPlayer_GetProfileCustomization_Request request) {
        return sendMessage(request, "GetProfileCustomization");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request) {
        return sendMessage(request, "GetPurchasedProfileCustomizations");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request) {
        return sendMessage(request, "GetPurchasedAndUpgradedProfileCustomizations");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request) {
        return sendMessage(request, "GetProfileThemesAvailable");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetProfileTheme(CPlayer_SetProfileTheme_Request request) {
        return sendMessage(request, "SetProfileTheme");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetProfilePreferences(CPlayer_SetProfilePreferences_Request request) {
        return sendMessage(request, "SetProfilePreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> PostStatusToFriends(CPlayer_PostStatusToFriends_Request request) {
        return sendMessage(request, "PostStatusToFriends");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPostedStatus(CPlayer_GetPostedStatus_Request request) {
        return sendMessage(request, "GetPostedStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeletePostedStatus(CPlayer_DeletePostedStatus_Request request) {
        return sendMessage(request, "DeletePostedStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ClientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request) {
        return sendMessage(request, "ClientGetLastPlayedTimes");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request) {
        return sendMessage(request, "GetTimeSSAAccepted");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AcceptSSA(CPlayer_AcceptSSA_Request request) {
        return sendMessage(request, "AcceptSSA");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetNicknameList(CPlayer_GetNicknameList_Request request) {
        return sendMessage(request, "GetNicknameList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request) {
        return sendMessage(request, "GetPerFriendPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request) {
        return sendMessage(request, "SetPerFriendPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AddFriend(CPlayer_AddFriend_Request request) {
        return sendMessage(request, "AddFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RemoveFriend(CPlayer_RemoveFriend_Request request) {
        return sendMessage(request, "RemoveFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> IgnoreFriend(CPlayer_IgnoreFriend_Request request) {
        return sendMessage(request, "IgnoreFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetCommunityPreferences(CPlayer_GetCommunityPreferences_Request request) {
        return sendMessage(request, "GetCommunityPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetCommunityPreferences(CPlayer_SetCommunityPreferences_Request request) {
        return sendMessage(request, "SetCommunityPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetTextFilterWords(CPlayer_GetTextFilterWords_Request request) {
        return sendMessage(request, "GetTextFilterWords");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request) {
        return sendMessage(request, "GetNewSteamAnnouncementState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request) {
        return sendMessage(request, "UpdateSteamAnnouncementLastRead");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPrivacySettings(CPlayer_GetPrivacySettings_Request request) {
        return sendMessage(request, "GetPrivacySettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetDurationControl(CPlayer_GetDurationControl_Request request) {
        return sendMessage(request, "GetDurationControl");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RecordDisconnectedPlaytime(CPlayer_RecordDisconnectedPlaytime_Request request) {
        return sendMessage(request, "RecordDisconnectedPlaytime");
    }
}
