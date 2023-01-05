package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IPlayerClient {

    /* NoResponse */
    void NotifyLastPlayedTimes(CPlayer_LastPlayedTimes_Notification request);

    /* NoResponse */
    void NotifyFriendNicknameChanged(CPlayer_FriendNicknameChanged_Notification request);

    /* NoResponse */
    void NotifyFriendEquippedProfileItemsChanged(CPlayer_FriendEquippedProfileItemsChanged_Notification request);

    /* NoResponse */
    void NotifyNewSteamAnnouncementState(CPlayer_NewSteamAnnouncementState_Notification request);

    /* NoResponse */
    void NotifyCommunityPreferencesChanged(CPlayer_CommunityPreferencesChanged_Notification request);

    /* NoResponse */
    void NotifyTextFilterWordsChanged(CPlayer_TextFilterWordsChanged_Notification request);

    /* NoResponse */
    void NotifyPerFriendPreferencesChanged(CPlayer_PerFriendPreferencesChanged_Notification request);

    /* NoResponse */
    void NotifyPrivacyPrivacySettingsChanged(CPlayer_PrivacySettingsChanged_Notification request);
}
