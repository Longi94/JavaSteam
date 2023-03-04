package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.rpc.IRemoteClientSteamClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class RemoteClientSteamClient extends UnifiedService implements IRemoteClientSteamClient {

    public RemoteClientSteamClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyRegisterStatusUpdate(CRemoteClient_RegisterStatusUpdate_Notification request) {
        sendNotification(request, "NotifyRegisterStatusUpdate");
    }

    @Override
    public void NotifyUnregisterStatusUpdate(CRemoteClient_UnregisterStatusUpdate_Notification request) {
        sendNotification(request, "NotifyUnregisterStatusUpdate");
    }

    @Override
    public void NotifyRemotePacket(CRemoteClient_RemotePacket_Notification request) {
        sendNotification(request, "NotifyRemotePacket");
    }

    @Override
    public void NotifySteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request) {
        sendNotification(request, "NotifySteamBroadcastPacket");
    }

    @Override
    public void NotifySteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request) {
        sendNotification(request, "NotifySteamToSteamPacket");
    }

    @Override
    public void NotifyRemotePlayTogetherPacket(CRemotePlayTogether_Notification request) {
        sendNotification(request, "NotifyRemotePlayTogetherPacket");
    }
}
