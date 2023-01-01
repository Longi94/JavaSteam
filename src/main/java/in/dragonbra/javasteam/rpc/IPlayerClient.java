package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IPlayerClient {
    NoResponse NotifyLastPlayedTimes(CPlayer_LastPlayedTimes_Notification request);

    NoResponse NotifyFriendNicknameChanged(CPlayer_FriendNicknameChanged_Notification request);

    NoResponse NotifyFriendEquippedProfileItemsChanged(CPlayer_FriendEquippedProfileItemsChanged_Notification request);

    NoResponse NotifyNewSteamAnnouncementState(CPlayer_NewSteamAnnouncementState_Notification request);

    NoResponse NotifyCommunityPreferencesChanged(CPlayer_CommunityPreferencesChanged_Notification request);

    NoResponse NotifyTextFilterWordsChanged(CPlayer_TextFilterWordsChanged_Notification request);

    NoResponse NotifyPerFriendPreferencesChanged(CPlayer_PerFriendPreferencesChanged_Notification request);

    NoResponse NotifyPrivacyPrivacySettingsChanged(CPlayer_PrivacySettingsChanged_Notification request);
}
