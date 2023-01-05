package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.CParental_ParentalLock_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.CParental_ParentalSettingsChange_Notification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.CParental_ParentalUnlock_Notification;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IParentalClient {

    /* NoResponse */
    void NotifySettingsChange(CParental_ParentalSettingsChange_Notification request);

    /* NoResponse */
    void NotifyUnlock(CParental_ParentalUnlock_Notification request);

    /* NoResponse */
    void NotifyLock(CParental_ParentalLock_Notification request);
}
