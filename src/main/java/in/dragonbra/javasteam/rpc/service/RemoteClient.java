package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.rpc.interfaces.IRemoteClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> GetPairingInfo(CRemoteClient_GetPairingInfo_Request request) {
        return sendMessage(request, "GetPairingInfo");
    }

    @Override
    public void NotifyOnline(CRemoteClient_Online_Notification request) {
        sendNotification(request, "NotifyOnline");

    }

    @Override
    public void NotifyReplyPacket(CRemoteClient_ReplyPacket_Notification request) {
        sendNotification(request, "NotifyReplyPacket");

    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AllocateTURNServer(CRemoteClient_AllocateTURNServer_Request request) {
        return sendMessage(request, "AllocateTURNServer");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AllocateRelayServer(CRemoteClient_AllocateRelayServer_Request request) {
        return sendMessage(request, "AllocateRelayServer");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AllocateSDR(CRemoteClient_AllocateSDR_Request request) {
        return sendMessage(request, "AllocateSDR");
    }

    @Override
    public void SendSteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request) {
        sendNotification(request, "SendSteamBroadcastPacket");
    }

    @Override
    public void SendSteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request) {
        sendNotification(request, "SendSteamToSteamPacket");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SendRemotePlaySessionStarted(CRemotePlay_SessionStarted_Request request) {
        return sendMessage(request, "SendRemotePlaySessionStarted");
    }

    @Override
    public void SendRemotePlaySessionStopped(CRemotePlay_SessionStopped_Notification request) {
        sendNotification(request, "SendRemotePlaySessionStopped");
    }

    @Override
    public void SendRemotePlayTogetherPacket(CRemotePlayTogether_Notification request) {
        sendNotification(request, "SendRemotePlayTogetherPacket");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> CreateRemotePlayTogetherInvitation(CRemoteClient_CreateRemotePlayTogetherInvitation_Request request) {
        return sendMessage(request, "CreateRemotePlayTogetherInvitation");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DeleteRemotePlayTogetherInvitation(CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request) {
        return sendMessage(request, "DeleteRemotePlayTogetherInvitation");
    }
}
