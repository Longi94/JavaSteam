package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IRemoteClientSteamClient {

    /* NoResponse */
    void NotifyRegisterStatusUpdate(CRemoteClient_RegisterStatusUpdate_Notification request);

    /* NoResponse */
    void NotifyUnregisterStatusUpdate(CRemoteClient_UnregisterStatusUpdate_Notification request);

    /* NoResponse */
    void NotifyRemotePacket(CRemoteClient_RemotePacket_Notification request);

    /* NoResponse */
    void NotifySteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request);

    /* NoResponse */
    void NotifySteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request);

    /* NoResponse */
    void NotifyRemotePlayTogetherPacket(CRemotePlayTogether_Notification request);
}
