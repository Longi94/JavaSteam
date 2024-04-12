package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IPlayer;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;
import org.jetbrains.annotations.NotNull;

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
    public AsyncJobSingle<ServiceMethodResponse> getPlayerLinkDetails(CPlayer_GetPlayerLinkDetails_Request request) {
        return sendMessage(request, "GetPlayerLinkDetails");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request) {
        return sendMessage(request, "GetMutualFriendsForIncomingInvites");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getOwnedGames(CPlayer_GetOwnedGames_Request request) {
        return sendMessage(request, "GetOwnedGames");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPlayNext(CPlayer_GetPlayNext_Request request) {
        return sendMessage(request, "GetPlayNext");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request) {
        return sendMessage(request, "GetFriendsGameplayInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request) {
        return sendMessage(request, "GetGameBadgeLevels");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getProfileBackground(CPlayer_GetProfileBackground_Request request) {
        return sendMessage(request, "GetProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setProfileBackground(CPlayer_SetProfileBackground_Request request) {
        return sendMessage(request, "SetProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request) {
        return sendMessage(request, "GetMiniProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request) {
        return sendMessage(request, "SetMiniProfileBackground");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAvatarFrame(CPlayer_GetAvatarFrame_Request request) {
        return sendMessage(request, "GetAvatarFrame");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setAvatarFrame(CPlayer_SetAvatarFrame_Request request) {
        return sendMessage(request, "SetAvatarFrame");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request) {
        return sendMessage(request, "GetAnimatedAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request) {
        return sendMessage(request, "SetAnimatedAvatar");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request, "GetSteamDeckKeyboardSkin");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request, "SetSteamDeckKeyboardSkin");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request) {
        return sendMessage(request, "GetProfileItemsOwned");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request) {
        return sendMessage(request, "GetProfileItemsEquipped");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request) {
        return sendMessage(request, "SetEquippedProfileItemFlags");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getEmoticonList(CPlayer_GetEmoticonList_Request request) {
        return sendMessage(request, "GetEmoticonList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAchievementsProgress(CPlayer_GetAchievementsProgress_Request request) {
        return sendMessage(request, "GetAchievementsProgress");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getFavoriteBadge(CPlayer_GetFavoriteBadge_Request request) {
        return sendMessage(request, "GetFavoriteBadge");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setFavoriteBadge(CPlayer_SetFavoriteBadge_Request request) {
        return sendMessage(request, "SetFavoriteBadge");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getProfileCustomization(CPlayer_GetProfileCustomization_Request request) {
        return sendMessage(request, "GetProfileCustomization");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request) {
        return sendMessage(request, "GetPurchasedProfileCustomizations");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request) {
        return sendMessage(request, "GetPurchasedAndUpgradedProfileCustomizations");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request) {
        return sendMessage(request, "GetProfileThemesAvailable");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setProfileTheme(CPlayer_SetProfileTheme_Request request) {
        return sendMessage(request, "SetProfileTheme");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setProfilePreferences(CPlayer_SetProfilePreferences_Request request) {
        return sendMessage(request, "SetProfilePreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> postStatusToFriends(CPlayer_PostStatusToFriends_Request request) {
        return sendMessage(request, "PostStatusToFriends");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPostedStatus(CPlayer_GetPostedStatus_Request request) {
        return sendMessage(request, "GetPostedStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> deletePostedStatus(CPlayer_DeletePostedStatus_Request request) {
        return sendMessage(request, "DeletePostedStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> clientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request) {
        return sendMessage(request, "ClientGetLastPlayedTimes");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request) {
        return sendMessage(request, "GetTimeSSAAccepted");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> acceptSSA(CPlayer_AcceptSSA_Request request) {
        return sendMessage(request, "AcceptSSA");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getNicknameList(CPlayer_GetNicknameList_Request request) {
        return sendMessage(request, "GetNicknameList");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request) {
        return sendMessage(request, "GetPerFriendPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request) {
        return sendMessage(request, "SetPerFriendPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> addFriend(CPlayer_AddFriend_Request request) {
        return sendMessage(request, "AddFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> removeFriend(CPlayer_RemoveFriend_Request request) {
        return sendMessage(request, "RemoveFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ignoreFriend(CPlayer_IgnoreFriend_Request request) {
        return sendMessage(request, "IgnoreFriend");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getCommunityPreferences(CPlayer_GetCommunityPreferences_Request request) {
        return sendMessage(request, "GetCommunityPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setCommunityPreferences(CPlayer_SetCommunityPreferences_Request request) {
        return sendMessage(request, "SetCommunityPreferences");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getTextFilterWords(CPlayer_GetTextFilterWords_Request request) {
        return sendMessage(request, "GetTextFilterWords");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request) {
        return sendMessage(request, "GetNewSteamAnnouncementState");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request) {
        return sendMessage(request, "UpdateSteamAnnouncementLastRead");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getPrivacySettings(CPlayer_GetPrivacySettings_Request request) {
        return sendMessage(request, "GetPrivacySettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getDurationControl(CPlayer_GetDurationControl_Request request) {
        return sendMessage(request, "GetDurationControl");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> recordDisconnectedPlaytime(CPlayer_RecordDisconnectedPlaytime_Request request) {
        return sendMessage(request, "RecordDisconnectedPlaytime");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getRecentPlaytimeSessionsForChild(SteammessagesPlayerSteamclient.CPlayer_GetRecentPlaytimeSessionsForChild_Request request) {
        return sendMessage(request, "GetRecentPlaytimeSessionsForChild");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getCommunityBadgeProgress(SteammessagesPlayerSteamclient.CPlayer_GetCommunityBadgeProgress_Request request) {
        return sendMessage(request, "GetCommunityBadgeProgress");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getTopAchievementsForGames(SteammessagesPlayerSteamclient.CPlayer_GetTopAchievementsForGames_Request request) {
        return sendMessage(request, "GetTopAchievementsForGames");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getGameAchievements(SteammessagesPlayerSteamclient.CPlayer_GetGameAchievements_Request request) {
        return sendMessage(request, "GetGameAchievements");
    }
}
