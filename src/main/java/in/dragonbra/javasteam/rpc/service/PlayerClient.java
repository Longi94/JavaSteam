package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesPlayerSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IPlayerClient;
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
    public void notifyLastPlayedTimes(CPlayer_LastPlayedTimes_Notification request) {
        sendNotification(request, "NotifyLastPlayedTimes");
    }

    @Override
    public void notifyFriendNicknameChanged(CPlayer_FriendNicknameChanged_Notification request) {
        sendNotification(request, "NotifyFriendNicknameChanged");
    }

    @Override
    public void notifyFriendEquippedProfileItemsChanged(CPlayer_FriendEquippedProfileItemsChanged_Notification request) {
        sendNotification(request, "NotifyFriendEquippedProfileItemsChanged");
    }

    @Override
    public void notifyNewSteamAnnouncementState(CPlayer_NewSteamAnnouncementState_Notification request) {
        sendNotification(request, "NotifyNewSteamAnnouncementState");
    }

    @Override
    public void notifyCommunityPreferencesChanged(CPlayer_CommunityPreferencesChanged_Notification request) {
        sendNotification(request, "NotifyCommunityPreferencesChanged");
    }

    @Override
    public void notifyTextFilterWordsChanged(CPlayer_TextFilterWordsChanged_Notification request) {
        sendNotification(request, "NotifyTextFilterWordsChanged");
    }

    @Override
    public void notifyPerFriendPreferencesChanged(CPlayer_PerFriendPreferencesChanged_Notification request) {
        sendNotification(request, "NotifyPerFriendPreferencesChanged");
    }

    @Override
    public void notifyPrivacyPrivacySettingsChanged(CPlayer_PrivacySettingsChanged_Notification request) {
        sendNotification(request, "NotifyPrivacyPrivacySettingsChanged");
    }
}
