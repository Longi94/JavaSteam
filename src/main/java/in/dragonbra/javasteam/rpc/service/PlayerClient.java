package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient;
import in.dragonbra.javasteam.rpc.IPlayerClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

// TODO implement

@SuppressWarnings("unused")
public class PlayerClient extends UnifiedService implements IPlayerClient {

    public PlayerClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyLastPlayedTimes(SteammessagesPlayerSteamclient.CPlayer_LastPlayedTimes_Notification request) {

    }

    @Override
    public void NotifyFriendNicknameChanged(SteammessagesPlayerSteamclient.CPlayer_FriendNicknameChanged_Notification request) {

    }

    @Override
    public void NotifyFriendEquippedProfileItemsChanged(SteammessagesPlayerSteamclient.CPlayer_FriendEquippedProfileItemsChanged_Notification request) {

    }

    @Override
    public void NotifyNewSteamAnnouncementState(SteammessagesPlayerSteamclient.CPlayer_NewSteamAnnouncementState_Notification request) {

    }

    @Override
    public void NotifyCommunityPreferencesChanged(SteammessagesPlayerSteamclient.CPlayer_CommunityPreferencesChanged_Notification request) {

    }

    @Override
    public void NotifyTextFilterWordsChanged(SteammessagesPlayerSteamclient.CPlayer_TextFilterWordsChanged_Notification request) {

    }

    @Override
    public void NotifyPerFriendPreferencesChanged(SteammessagesPlayerSteamclient.CPlayer_PerFriendPreferencesChanged_Notification request) {

    }

    @Override
    public void NotifyPrivacyPrivacySettingsChanged(SteammessagesPlayerSteamclient.CPlayer_PrivacySettingsChanged_Notification request) {

    }
}
