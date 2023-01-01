package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.NoResponse;

public interface IParentalClient {
    NoResponse NotifySettingsChange(CParental_ParentalSettingsChange_Notification request);

    NoResponse NotifyUnlock(CParental_ParentalUnlock_Notification request);

    NoResponse NotifyLock(CParental_ParentalLock_Notification request);
}
