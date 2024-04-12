package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IParentalClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ParentalClient extends UnifiedService implements IParentalClient {

    public ParentalClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void notifySettingsChange(CParental_ParentalSettingsChange_Notification request) {
        sendNotification(request, "NotifySettingsChange");
    }

    @Override
    public void notifyUnlock(CParental_ParentalUnlock_Notification request) {
        sendNotification(request, "NotifyUnlock");
    }

    @Override
    public void notifyLock(CParental_ParentalLock_Notification request) {
        sendNotification(request, "NotifyLock");
    }

    @Override
    public void notifyPlaytimeUsed(SteammessagesParentalSteamclient.CParental_PlaytimeUsed_Notification request) {
        sendNotification(request, "NotifyPlaytimeUsed");
    }
}
