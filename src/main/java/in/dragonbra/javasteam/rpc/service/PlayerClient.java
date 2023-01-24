package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.IPlayerClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class PlayerClient extends UnifiedService implements IPlayerClient {

    public PlayerClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyLastPlayedTimes(CPlayer_LastPlayedTimes_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyFriendNicknameChanged(CPlayer_FriendNicknameChanged_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyFriendEquippedProfileItemsChanged(CPlayer_FriendEquippedProfileItemsChanged_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyNewSteamAnnouncementState(CPlayer_NewSteamAnnouncementState_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyCommunityPreferencesChanged(CPlayer_CommunityPreferencesChanged_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyTextFilterWordsChanged(CPlayer_TextFilterWordsChanged_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyPerFriendPreferencesChanged(CPlayer_PerFriendPreferencesChanged_Notification request) {
        sendNotification(request);
    }

    @Override
    public void NotifyPrivacyPrivacySettingsChanged(CPlayer_PrivacySettingsChanged_Notification request) {
        sendNotification(request);
    }
}
