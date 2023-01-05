package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages;
import in.dragonbra.javasteam.rpc.IRemoteClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement

@SuppressWarnings("unused")
public class RemoteClient extends UnifiedService implements IRemoteClient {

    public RemoteClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetPairingInfo(SteammessagesRemoteclientServiceMessages.CRemoteClient_GetPairingInfo_Request request) {
        return null;
    }

    @Override
    public void NotifyOnline(SteammessagesRemoteclientServiceMessages.CRemoteClient_Online_Notification request) {

    }

    @Override
    public void NotifyReplyPacket(SteammessagesRemoteclientServiceMessages.CRemoteClient_ReplyPacket_Notification request) {

    }

    @Override
    public JobID AllocateTURNServer(SteammessagesRemoteclientServiceMessages.CRemoteClient_AllocateTURNServer_Request request) {
        return null;
    }

    @Override
    public JobID AllocateRelayServer(SteammessagesRemoteclientServiceMessages.CRemoteClient_AllocateRelayServer_Request request) {
        return null;
    }

    @Override
    public JobID AllocateSDR(SteammessagesRemoteclientServiceMessages.CRemoteClient_AllocateSDR_Request request) {
        return null;
    }

    @Override
    public void SendSteamBroadcastPacket(SteammessagesRemoteclientServiceMessages.CRemoteClient_SteamBroadcast_Notification request) {

    }

    @Override
    public void SendSteamToSteamPacket(SteammessagesRemoteclientServiceMessages.CRemoteClient_SteamToSteam_Notification request) {

    }

    @Override
    public JobID SendRemotePlaySessionStarted(SteammessagesRemoteclientServiceMessages.CRemotePlay_SessionStarted_Request request) {
        return null;
    }

    @Override
    public void SendRemotePlaySessionStopped(SteammessagesRemoteclientServiceMessages.CRemotePlay_SessionStopped_Notification request) {

    }

    @Override
    public void SendRemotePlayTogetherPacket(SteammessagesRemoteclientServiceMessages.CRemotePlayTogether_Notification request) {

    }

    @Override
    public JobID CreateRemotePlayTogetherInvitation(SteammessagesRemoteclientServiceMessages.CRemoteClient_CreateRemotePlayTogetherInvitation_Request request) {
        return null;
    }

    @Override
    public JobID DeleteRemotePlayTogetherInvitation(SteammessagesRemoteclientServiceMessages.CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request) {
        return null;
    }
}
