package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IPlayer {
    /* CPlayer_GetMutualFriendsForIncomingInvites_Response  */
    JobID GetMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request);

    /* CPlayer_GetOwnedGames_Response  */
    JobID GetOwnedGames(CPlayer_GetOwnedGames_Request request);

    /* CPlayer_GetPlayNext_Response  */
    JobID GetPlayNext(CPlayer_GetPlayNext_Request request);

    /* CPlayer_GetFriendsGameplayInfo_Response  */
    JobID GetFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request);

    /* CPlayer_GetGameBadgeLevels_Response  */
    JobID GetGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request);

    /* CPlayer_GetProfileBackground_Response  */
    JobID GetProfileBackground(CPlayer_GetProfileBackground_Request request);

    /* CPlayer_SetProfileBackground_Response  */
    JobID SetProfileBackground(CPlayer_SetProfileBackground_Request request);

    /* CPlayer_GetMiniProfileBackground_Response  */
    JobID GetMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request);

    /* CPlayer_SetMiniProfileBackground_Response  */
    JobID SetMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request);

    /* CPlayer_GetAvatarFrame_Response  */
    JobID GetAvatarFrame(CPlayer_GetAvatarFrame_Request request);

    /* CPlayer_SetAvatarFrame_Response  */
    JobID SetAvatarFrame(CPlayer_SetAvatarFrame_Request request);

    /* CPlayer_GetAnimatedAvatar_Response  */
    JobID GetAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request);

    /* CPlayer_SetAnimatedAvatar_Response  */
    JobID SetAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request);

    /* CPlayer_GetSteamDeckKeyboardSkin_Response  */
    JobID GetSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request);

    /* CPlayer_SetSteamDeckKeyboardSkin_Response  */
    JobID SetSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request);

    /* CPlayer_GetProfileItemsOwned_Response  */
    JobID GetProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request);

    /* CPlayer_GetProfileItemsEquipped_Response  */
    JobID GetProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request);

    /* CPlayer_SetEquippedProfileItemFlags_Response  */
    JobID SetEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request);

    /* CPlayer_GetEmoticonList_Response  */
    JobID GetEmoticonList(CPlayer_GetEmoticonList_Request request);

    /* CPlayer_GetAchievementsProgress_Response  */
    JobID GetAchievementsProgress(CPlayer_GetAchievementsProgress_Request request);

    /* CPlayer_GetFavoriteBadge_Response  */
    JobID GetFavoriteBadge(CPlayer_GetFavoriteBadge_Request request);

    /* CPlayer_SetFavoriteBadge_Response  */
    JobID SetFavoriteBadge(CPlayer_SetFavoriteBadge_Request request);

    /* CPlayer_GetProfileCustomization_Response  */
    JobID GetProfileCustomization(CPlayer_GetProfileCustomization_Request request);

    /* CPlayer_GetPurchasedProfileCustomizations_Response  */
    JobID GetPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request);

    /* CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Response  */
    JobID GetPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request);

    /* CPlayer_GetProfileThemesAvailable_Response  */
    JobID GetProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request);

    /* CPlayer_SetProfileTheme_Response  */
    JobID SetProfileTheme(CPlayer_SetProfileTheme_Request request);

    /* CPlayer_SetProfilePreferences_Response  */
    JobID SetProfilePreferences(CPlayer_SetProfilePreferences_Request request);

    /* CPlayer_PostStatusToFriends_Response  */
    JobID PostStatusToFriends(CPlayer_PostStatusToFriends_Request request);

    /* CPlayer_GetPostedStatus_Response  */
    JobID GetPostedStatus(CPlayer_GetPostedStatus_Request request);

    /* CPlayer_DeletePostedStatus_Response  */
    JobID DeletePostedStatus(CPlayer_DeletePostedStatus_Request request);

    /* CPlayer_GetLastPlayedTimes_Response  */
    JobID ClientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request);

    /* CPlayer_GetTimeSSAAccepted_Response  */
    JobID GetTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request);

    /* CPlayer_AcceptSSA_Response  */
    JobID AcceptSSA(CPlayer_AcceptSSA_Request request);

    /* CPlayer_GetNicknameList_Response  */
    JobID GetNicknameList(CPlayer_GetNicknameList_Request request);

    /* CPlayer_GetPerFriendPreferences_Response  */
    JobID GetPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request);

    /* CPlayer_SetPerFriendPreferences_Response  */
    JobID SetPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request);

    /* CPlayer_AddFriend_Response  */
    JobID AddFriend(CPlayer_AddFriend_Request request);

    /* CPlayer_RemoveFriend_Response  */
    JobID RemoveFriend(CPlayer_RemoveFriend_Request request);

    /* CPlayer_IgnoreFriend_Response  */
    JobID IgnoreFriend(CPlayer_IgnoreFriend_Request request);

    /* CPlayer_GetCommunityPreferences_Response  */
    JobID GetCommunityPreferences(CPlayer_GetCommunityPreferences_Request request);

    /* CPlayer_SetCommunityPreferences_Response  */
    JobID SetCommunityPreferences(CPlayer_SetCommunityPreferences_Request request);

    /* CPlayer_GetTextFilterWords_Response  */
    JobID GetTextFilterWords(CPlayer_GetTextFilterWords_Request request);

    /* CPlayer_GetNewSteamAnnouncementState_Response  */
    JobID GetNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request);

    /* CPlayer_UpdateSteamAnnouncementLastRead_Response  */
    JobID UpdateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request);

    /* CPlayer_GetPrivacySettings_Response  */
    JobID GetPrivacySettings(CPlayer_GetPrivacySettings_Request request);

    /* CPlayer_GetDurationControl_Response  */
    JobID GetDurationControl(CPlayer_GetDurationControl_Request request);
}
