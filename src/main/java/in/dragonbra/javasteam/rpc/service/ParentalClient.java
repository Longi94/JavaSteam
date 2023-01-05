package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient;
import in.dragonbra.javasteam.rpc.IParentalClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

// TODO implement

@SuppressWarnings("unused")
public class ParentalClient extends UnifiedService implements IParentalClient {

    public ParentalClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifySettingsChange(SteammessagesParentalSteamclient.CParental_ParentalSettingsChange_Notification request) {

    }

    @Override
    public void NotifyUnlock(SteammessagesParentalSteamclient.CParental_ParentalUnlock_Notification request) {

    }

    @Override
    public void NotifyLock(SteammessagesParentalSteamclient.CParental_ParentalLock_Notification request) {

    }
}
