package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CCloudGaming_CreateNonce_Request;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CCloudGaming_GetTimeRemaining_Request;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface ICloudGaming {

    /* CCloudGaming_CreateNonce_Response */
    JobID CreateNonce(CCloudGaming_CreateNonce_Request request);

    /* CCloudGaming_GetTimeRemaining_Response */
    JobID GetTimeRemaining(CCloudGaming_GetTimeRemaining_Request request);
}
