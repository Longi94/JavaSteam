package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IParentalClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

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
    public void NotifySettingsChange(CParental_ParentalSettingsChange_Notification request) {
        sendNotification(request, "NotifySettingsChange");
    }

    @Override
    public void NotifyUnlock(CParental_ParentalUnlock_Notification request) {
        sendNotification(request, "NotifyUnlock");
    }

    @Override
    public void NotifyLock(CParental_ParentalLock_Notification request) {
        sendNotification(request, "NotifyLock");
    }
}
