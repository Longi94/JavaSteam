package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IPlayer {
    /* CPlayer_GetPlayerLinkDetails_Response */
    AsyncJobSingle<ServiceMethodResponse> GetPlayerLinkDetails(CPlayer_GetPlayerLinkDetails_Request request);

    /* CPlayer_GetMutualFriendsForIncomingInvites_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetMutualFriendsForIncomingInvites(CPlayer_GetMutualFriendsForIncomingInvites_Request request);

    /* CPlayer_GetOwnedGames_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetOwnedGames(CPlayer_GetOwnedGames_Request request);

    /* CPlayer_GetPlayNext_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPlayNext(CPlayer_GetPlayNext_Request request);

    /* CPlayer_GetFriendsGameplayInfo_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetFriendsGameplayInfo(CPlayer_GetFriendsGameplayInfo_Request request);

    /* CPlayer_GetGameBadgeLevels_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetGameBadgeLevels(CPlayer_GetGameBadgeLevels_Request request);

    /* CPlayer_GetProfileBackground_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetProfileBackground(CPlayer_GetProfileBackground_Request request);

    /* CPlayer_SetProfileBackground_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetProfileBackground(CPlayer_SetProfileBackground_Request request);

    /* CPlayer_GetMiniProfileBackground_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetMiniProfileBackground(CPlayer_GetMiniProfileBackground_Request request);

    /* CPlayer_SetMiniProfileBackground_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetMiniProfileBackground(CPlayer_SetMiniProfileBackground_Request request);

    /* CPlayer_GetAvatarFrame_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetAvatarFrame(CPlayer_GetAvatarFrame_Request request);

    /* CPlayer_SetAvatarFrame_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetAvatarFrame(CPlayer_SetAvatarFrame_Request request);

    /* CPlayer_GetAnimatedAvatar_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetAnimatedAvatar(CPlayer_GetAnimatedAvatar_Request request);

    /* CPlayer_SetAnimatedAvatar_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetAnimatedAvatar(CPlayer_SetAnimatedAvatar_Request request);

    /* CPlayer_GetSteamDeckKeyboardSkin_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetSteamDeckKeyboardSkin(CPlayer_GetSteamDeckKeyboardSkin_Request request);

    /* CPlayer_SetSteamDeckKeyboardSkin_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetSteamDeckKeyboardSkin(CPlayer_SetSteamDeckKeyboardSkin_Request request);

    /* CPlayer_GetProfileItemsOwned_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetProfileItemsOwned(CPlayer_GetProfileItemsOwned_Request request);

    /* CPlayer_GetProfileItemsEquipped_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetProfileItemsEquipped(CPlayer_GetProfileItemsEquipped_Request request);

    /* CPlayer_SetEquippedProfileItemFlags_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetEquippedProfileItemFlags(CPlayer_SetEquippedProfileItemFlags_Request request);

    /* CPlayer_GetEmoticonList_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetEmoticonList(CPlayer_GetEmoticonList_Request request);

    /* CPlayer_GetAchievementsProgress_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetAchievementsProgress(CPlayer_GetAchievementsProgress_Request request);

    /* CPlayer_GetFavoriteBadge_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetFavoriteBadge(CPlayer_GetFavoriteBadge_Request request);

    /* CPlayer_SetFavoriteBadge_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetFavoriteBadge(CPlayer_SetFavoriteBadge_Request request);

    /* CPlayer_GetProfileCustomization_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetProfileCustomization(CPlayer_GetProfileCustomization_Request request);

    /* CPlayer_GetPurchasedProfileCustomizations_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPurchasedProfileCustomizations(CPlayer_GetPurchasedProfileCustomizations_Request request);

    /* CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPurchasedAndUpgradedProfileCustomizations(CPlayer_GetPurchasedAndUpgradedProfileCustomizations_Request request);

    /* CPlayer_GetProfileThemesAvailable_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetProfileThemesAvailable(CPlayer_GetProfileThemesAvailable_Request request);

    /* CPlayer_SetProfileTheme_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetProfileTheme(CPlayer_SetProfileTheme_Request request);

    /* CPlayer_SetProfilePreferences_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetProfilePreferences(CPlayer_SetProfilePreferences_Request request);

    /* CPlayer_PostStatusToFriends_Response  */
    AsyncJobSingle<ServiceMethodResponse> PostStatusToFriends(CPlayer_PostStatusToFriends_Request request);

    /* CPlayer_GetPostedStatus_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPostedStatus(CPlayer_GetPostedStatus_Request request);

    /* CPlayer_DeletePostedStatus_Response  */
    AsyncJobSingle<ServiceMethodResponse> DeletePostedStatus(CPlayer_DeletePostedStatus_Request request);

    /* CPlayer_GetLastPlayedTimes_Response  */
    AsyncJobSingle<ServiceMethodResponse> ClientGetLastPlayedTimes(CPlayer_GetLastPlayedTimes_Request request);

    /* CPlayer_GetTimeSSAAccepted_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetTimeSSAAccepted(CPlayer_GetTimeSSAAccepted_Request request);

    /* CPlayer_AcceptSSA_Response  */
    AsyncJobSingle<ServiceMethodResponse> AcceptSSA(CPlayer_AcceptSSA_Request request);

    /* CPlayer_GetNicknameList_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetNicknameList(CPlayer_GetNicknameList_Request request);

    /* CPlayer_GetPerFriendPreferences_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPerFriendPreferences(CPlayer_GetPerFriendPreferences_Request request);

    /* CPlayer_SetPerFriendPreferences_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetPerFriendPreferences(CPlayer_SetPerFriendPreferences_Request request);

    /* CPlayer_AddFriend_Response  */
    AsyncJobSingle<ServiceMethodResponse> AddFriend(CPlayer_AddFriend_Request request);

    /* CPlayer_RemoveFriend_Response  */
    AsyncJobSingle<ServiceMethodResponse> RemoveFriend(CPlayer_RemoveFriend_Request request);

    /* CPlayer_IgnoreFriend_Response  */
    AsyncJobSingle<ServiceMethodResponse> IgnoreFriend(CPlayer_IgnoreFriend_Request request);

    /* CPlayer_GetCommunityPreferences_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetCommunityPreferences(CPlayer_GetCommunityPreferences_Request request);

    /* CPlayer_SetCommunityPreferences_Response  */
    AsyncJobSingle<ServiceMethodResponse> SetCommunityPreferences(CPlayer_SetCommunityPreferences_Request request);

    /* CPlayer_GetTextFilterWords_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetTextFilterWords(CPlayer_GetTextFilterWords_Request request);

    /* CPlayer_GetNewSteamAnnouncementState_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetNewSteamAnnouncementState(CPlayer_GetNewSteamAnnouncementState_Request request);

    /* CPlayer_UpdateSteamAnnouncementLastRead_Response  */
    AsyncJobSingle<ServiceMethodResponse> UpdateSteamAnnouncementLastRead(CPlayer_UpdateSteamAnnouncementLastRead_Request request);

    /* CPlayer_GetPrivacySettings_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetPrivacySettings(CPlayer_GetPrivacySettings_Request request);

    /* CPlayer_GetDurationControl_Response  */
    AsyncJobSingle<ServiceMethodResponse> GetDurationControl(CPlayer_GetDurationControl_Request request);

    /* CPlayer_RecordDisconnectedPlaytime_Response */
    AsyncJobSingle<ServiceMethodResponse> RecordDisconnectedPlaytime(CPlayer_RecordDisconnectedPlaytime_Request request);
}
