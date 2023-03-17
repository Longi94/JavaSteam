package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CCloudGaming_CreateNonce_Request;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.CCloudGaming_GetTimeRemaining_Request;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface ICloudGaming {

    /* CCloudGaming_CreateNonce_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateNonce(CCloudGaming_CreateNonce_Request request);

    /* CCloudGaming_GetTimeRemaining_Response */
    AsyncJobSingle<ServiceMethodResponse> GetTimeRemaining(CCloudGaming_GetTimeRemaining_Request request);
}
