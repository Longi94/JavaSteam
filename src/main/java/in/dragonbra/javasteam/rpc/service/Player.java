package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.IPlayer;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

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
    public JobID GetMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetOwnedGames(CPlayer_GetOwnedGames_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPlayNext(CPlayer_GetPlayNext_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetProfileBackground(CPlayer_GetProfileBackground_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetProfileBackground(CPlayer_SetProfileBackground_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetAvatarFrame(CPlayer_GetAvatarFrame_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetAvatarFrame(CPlayer_SetAvatarFrame_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetEmoticonList(CPlayer_GetEmoticonList_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetAchievementsProgress(CPlayer_GetAchievementsProgress_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetFavoriteBadge(CPlayer_GetFavoriteBadge_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetFavoriteBadge(CPlayer_SetFavoriteBadge_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetProfileCustomization(CPlayer_GetProfileCustomization_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetProfileTheme(CPlayer_SetProfileTheme_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetProfilePreferences(CPlayer_SetProfilePreferences_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID PostStatusToFriends(CPlayer_PostStatusToFriends_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPostedStatus(CPlayer_GetPostedStatus_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID DeletePostedStatus(CPlayer_DeletePostedStatus_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID ClientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID AcceptSSA(CPlayer_AcceptSSA_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetNicknameList(CPlayer_GetNicknameList_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID AddFriend(CPlayer_AddFriend_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID RemoveFriend(CPlayer_RemoveFriend_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID IgnoreFriend(CPlayer_IgnoreFriend_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetCommunityPreferences(CPlayer_GetCommunityPreferences_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetCommunityPreferences(CPlayer_SetCommunityPreferences_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetTextFilterWords(CPlayer_GetTextFilterWords_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID UpdateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetPrivacySettings(CPlayer_GetPrivacySettings_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetDurationControl(CPlayer_GetDurationControl_Request request) {
        return sendMessage(request);
    }
}
