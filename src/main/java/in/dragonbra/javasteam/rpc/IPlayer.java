package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;

public interface IPlayer {
    CPlayer_GetMutualFriendsForIncomingInvites_Response GetMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request);

    CPlayer_GetOwnedGames_Response GetOwnedGames(CPlayer_GetOwnedGames_Request request);

    CPlayer_GetPlayNext_Response GetPlayNext(CPlayer_GetPlayNext_Request request);

    CPlayer_GetFriendsGameplayInfo_Response GetFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request);

    CPlayer_GetGameBadgeLevels_Response GetGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request);

    CPlayer_GetProfileBackground_Response GetProfileBackground(CPlayer_GetProfileBackground_Request request);

    CPlayer_SetProfileBackground_Response SetProfileBackground(CPlayer_SetProfileBackground_Request request);

    CPlayer_GetMiniProfileBackground_Response GetMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request);

    CPlayer_SetMiniProfileBackground_Response SetMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request);

    CPlayer_GetAvatarFrame_Response GetAvatarFrame(CPlayer_GetAvatarFrame_Request request);

    CPlayer_SetAvatarFrame_Response SetAvatarFrame(CPlayer_SetAvatarFrame_Request request);

    CPlayer_GetAnimatedAvatar_Response GetAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request);

    CPlayer_SetAnimatedAvatar_Response SetAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request);

    CPlayer_GetSteamDeckKeyboardSkin_Response GetSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request);

    CPlayer_SetSteamDeckKeyboardSkin_Response SetSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request);

    CPlayer_GetProfileItemsOwned_Response GetProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request);

    CPlayer_GetProfileItemsEquipped_Response GetProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request);

    CPlayer_SetEquippedProfileItemFlags_Response SetEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request);

    CPlayer_GetEmoticonList_Response GetEmoticonList(CPlayer_GetEmoticonList_Request request);

    CPlayer_GetAchievementsProgress_Response GetAchievementsProgress(CPlayer_GetAchievementsProgress_Request request);

    CPlayer_GetFavoriteBadge_Response GetFavoriteBadge(CPlayer_GetFavoriteBadge_Request request);

    CPlayer_SetFavoriteBadge_Response SetFavoriteBadge(CPlayer_SetFavoriteBadge_Request request);

    CPlayer_GetProfileCustomization_Response GetProfileCustomization(CPlayer_GetProfileCustomization_Request request);

    CPlayer_GetPurchasedProfileCustomizations_Response GetPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request);

    CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Response GetPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request);

    CPlayer_GetProfileThemesAvailable_Response GetProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request);

    CPlayer_SetProfileTheme_Response SetProfileTheme(CPlayer_SetProfileTheme_Request request);

    CPlayer_SetProfilePreferences_Response SetProfilePreferences(CPlayer_SetProfilePreferences_Request request);

    CPlayer_PostStatusToFriends_Response PostStatusToFriends(CPlayer_PostStatusToFriends_Request request);

    CPlayer_GetPostedStatus_Response GetPostedStatus(CPlayer_GetPostedStatus_Request request);

    CPlayer_DeletePostedStatus_Response DeletePostedStatus(CPlayer_DeletePostedStatus_Request request);

    CPlayer_GetLastPlayedTimes_Response ClientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request);

    CPlayer_GetTimeSSAAccepted_Response GetTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request);

    CPlayer_AcceptSSA_Response AcceptSSA(CPlayer_AcceptSSA_Request request);

    CPlayer_GetNicknameList_Response GetNicknameList(CPlayer_GetNicknameList_Request request);

    CPlayer_GetPerFriendPreferences_Response GetPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request);

    CPlayer_SetPerFriendPreferences_Response SetPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request);

    CPlayer_AddFriend_Response AddFriend(CPlayer_AddFriend_Request request);

    CPlayer_RemoveFriend_Response RemoveFriend(CPlayer_RemoveFriend_Request request);

    CPlayer_IgnoreFriend_Response IgnoreFriend(CPlayer_IgnoreFriend_Request request);

    CPlayer_GetCommunityPreferences_Response GetCommunityPreferences(CPlayer_GetCommunityPreferences_Request request);

    CPlayer_SetCommunityPreferences_Response SetCommunityPreferences(CPlayer_SetCommunityPreferences_Request request);

    CPlayer_GetTextFilterWords_Response GetTextFilterWords(CPlayer_GetTextFilterWords_Request request);

    CPlayer_GetNewSteamAnnouncementState_Response GetNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request);

    CPlayer_UpdateSteamAnnouncementLastRead_Response UpdateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request);

    CPlayer_GetPrivacySettings_Response GetPrivacySettings(CPlayer_GetPrivacySettings_Request request);

    CPlayer_GetDurationControl_Response GetDurationControl(CPlayer_GetDurationControl_Request request);
}
