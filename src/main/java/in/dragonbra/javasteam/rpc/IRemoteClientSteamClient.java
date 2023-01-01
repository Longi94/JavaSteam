package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IRemoteClientSteamClient {
    NoResponse NotifyRegisterStatusUpdate(CRemoteClient_RegisterStatusUpdate_Notification request);

    NoResponse NotifyUnregisterStatusUpdate(CRemoteClient_UnregisterStatusUpdate_Notification request);

    NoResponse NotifyRemotePacket(CRemoteClient_RemotePacket_Notification request);

    NoResponse NotifySteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request);

    NoResponse NotifySteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request);

    NoResponse NotifyRemotePlayTogetherPacket(CRemotePlayTogether_Notification request);
}
