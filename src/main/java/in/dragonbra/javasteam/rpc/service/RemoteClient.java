package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.rpc.IRemoteClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class RemoteClient extends UnifiedService implements IRemoteClient {

    public RemoteClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetPairingInfo(CRemoteClient_GetPairingInfo_Request request) {
        return sendMessage(request);
    }

    @Override
    public void NotifyOnline(CRemoteClient_Online_Notification request) {
        sendNotification(request);

    }

    @Override
    public void NotifyReplyPacket(CRemoteClient_ReplyPacket_Notification request) {
        sendNotification(request);

    }

    @Override
    public JobID AllocateTURNServer(CRemoteClient_AllocateTURNServer_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID AllocateRelayServer(CRemoteClient_AllocateRelayServer_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID AllocateSDR(CRemoteClient_AllocateSDR_Request request) {
        return sendMessage(request);
    }

    @Override
    public void SendSteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request) {
        sendNotification(request);
    }

    @Override
    public void SendSteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request) {
        sendNotification(request);
    }

    @Override
    public JobID SendRemotePlaySessionStarted(CRemotePlay_SessionStarted_Request request) {
        return sendMessage(request);
    }

    @Override
    public void SendRemotePlaySessionStopped(CRemotePlay_SessionStopped_Notification request) {
        sendNotification(request);
    }

    @Override
    public void SendRemotePlayTogetherPacket(CRemotePlayTogether_Notification request) {
        sendNotification(request);
    }

    @Override
    public JobID CreateRemotePlayTogetherInvitation(CRemoteClient_CreateRemotePlayTogetherInvitation_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID DeleteRemotePlayTogetherInvitation(CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request) {
        return sendMessage(request);
    }
}
