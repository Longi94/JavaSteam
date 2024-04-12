package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.rpc.interfaces.IRemoteClientSteamClient;
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
    public void notifyRegisterStatusUpdate(CRemoteClient_RegisterStatusUpdate_Notification request) {
        sendNotification(request, "NotifyRegisterStatusUpdate");
    }

    @Override
    public void notifyUnregisterStatusUpdate(CRemoteClient_UnregisterStatusUpdate_Notification request) {
        sendNotification(request, "NotifyUnregisterStatusUpdate");
    }

    @Override
    public void notifyRemotePacket(CRemoteClient_RemotePacket_Notification request) {
        sendNotification(request, "NotifyRemotePacket");
    }

    @Override
    public void notifySteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request) {
        sendNotification(request, "NotifySteamBroadcastPacket");
    }

    @Override
    public void notifySteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request) {
        sendNotification(request, "NotifySteamToSteamPacket");
    }

    @Override
    public void notifyRemotePlayTogetherPacket(CRemotePlayTogether_Notification request) {
        sendNotification(request, "NotifyRemotePlayTogetherPacket");
    }
}
