package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IRemoteClient {

    /* CRemoteClient_GetPairingInfo_Response */
    JobID GetPairingInfo(CRemoteClient_GetPairingInfo_Request request);

    /* NoResponse */
    void NotifyOnline(CRemoteClient_Online_Notification request);

    /* NoResponse */
    void NotifyReplyPacket(CRemoteClient_ReplyPacket_Notification request);

    /* CRemoteClient_AllocateTURNServer_Response */
    JobID AllocateTURNServer(CRemoteClient_AllocateTURNServer_Request request);

    /* CRemoteClient_AllocateRelayServer_Response */
    JobID AllocateRelayServer(CRemoteClient_AllocateRelayServer_Request request);

    /* CRemoteClient_AllocateSDR_Response */
    JobID AllocateSDR(CRemoteClient_AllocateSDR_Request request);

    /* NoResponse */
    void SendSteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request);

    /* NoResponse */
    void SendSteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request);

    /* CRemotePlay_SessionStarted_Response */
    JobID SendRemotePlaySessionStarted(CRemotePlay_SessionStarted_Request request);

    /* NoResponse */
    void SendRemotePlaySessionStopped(CRemotePlay_SessionStopped_Notification request);

    /* NoResponse */
    void SendRemotePlayTogetherPacket(CRemotePlayTogether_Notification request);

    /* CRemoteClient_CreateRemotePlayTogetherInvitation_Response */
    JobID CreateRemotePlayTogetherInvitation(CRemoteClient_CreateRemotePlayTogetherInvitation_Request request);

    /* CRemoteClient_DeleteRemotePlayTogetherInvitation_Response */
    JobID DeleteRemotePlayTogetherInvitation(CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request);
}
