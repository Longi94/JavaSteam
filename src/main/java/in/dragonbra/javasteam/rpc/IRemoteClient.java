package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesRemoteclientServiceMessages.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IRemoteClient {
    CRemoteClient_GetPairingInfo_Response GetPairingInfo(CRemoteClient_GetPairingInfo_Request request);

    NoResponse NotifyOnline(CRemoteClient_Online_Notification request);

    NoResponse NotifyReplyPacket(CRemoteClient_ReplyPacket_Notification request);

    CRemoteClient_AllocateTURNServer_Response AllocateTURNServer(CRemoteClient_AllocateTURNServer_Request request);

    CRemoteClient_AllocateRelayServer_Response AllocateRelayServer(CRemoteClient_AllocateRelayServer_Request request);

    CRemoteClient_AllocateSDR_Response AllocateSDR(CRemoteClient_AllocateSDR_Request request);

    NoResponse SendSteamBroadcastPacket(CRemoteClient_SteamBroadcast_Notification request);

    NoResponse SendSteamToSteamPacket(CRemoteClient_SteamToSteam_Notification request);

    CRemotePlay_SessionStarted_Response SendRemotePlaySessionStarted(CRemotePlay_SessionStarted_Request request);

    NoResponse SendRemotePlaySessionStopped(CRemotePlay_SessionStopped_Notification request);

    NoResponse SendRemotePlayTogetherPacket(CRemotePlayTogether_Notification request);

    CRemoteClient_CreateRemotePlayTogetherInvitation_Response CreateRemotePlayTogetherInvitation(CRemoteClient_CreateRemotePlayTogetherInvitation_Request request);

    CRemoteClient_DeleteRemotePlayTogetherInvitation_Response DeleteRemotePlayTogetherInvitation(CRemoteClient_DeleteRemotePlayTogetherInvitation_Request request);
}
