package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IRemoteClient {

    /* CRemoteClient_GetPairingInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetPairingInfo(CRemoteClient_GetPairingInfo_Request request);

    /* NoResponse */
    void NotifyOnline(CRemoteClient_Online_Notification request);

    /* NoResponse */
    void NotifyReplyPacket(CRemoteClient_ReplyPacket_Notification request);

    /* CRemoteClient_AllocateRelayServer_Response */
    AsyncJobSingle<ServiceMethodResponse> AllocateRelayServer(CRemoteClient_AllocateRelayServer_Request request);

    /* CRemoteClient_AllocateSDR_Response */
    AsyncJobSingle<ServiceMethodResponse> AllocateSDR(CRemoteClient_AllocateSDR_Request request);

    /* NoResponse */
    void SendSteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request);

    /* NoResponse */
    void SendSteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request);

    /* CRemotePlay_SessionStarted_Response */
    AsyncJobSingle<ServiceMethodResponse> SendRemotePlaySessionStarted(CRemotePlay_SessionStarted_Request request);

    /* NoResponse */
    void SendRemotePlaySessionStopped(CRemotePlay_SessionStopped_Notification request);

    /* NoResponse */
    void SendRemotePlayTogetherPacket(CRemotePlayTogether_Notification request);

    /* CRemoteClient_CreateRemotePlayTogetherInvitation_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateRemotePlayTogetherInvitation(CRemoteClient_CreateRemotePlayTogetherInvitation_Request request);

    /* CRemoteClient_DeleteRemotePlayTogetherInvitation_Response */
    AsyncJobSingle<ServiceMethodResponse> DeleteRemotePlayTogetherInvitation(CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request);
}
