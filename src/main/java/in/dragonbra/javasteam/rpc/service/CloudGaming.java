package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.interfaces.ICloudGaming;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public class CloudGaming extends UnifiedService implements ICloudGaming {

    public CloudGaming(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> createNonce(SteammessagesAuthSteamclient.CCloudGaming_CreateNonce_Request request) {
        return sendMessage(request, "CreateNonce");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getTimeRemaining(SteammessagesAuthSteamclient.CCloudGaming_GetTimeRemaining_Request request) {
        return sendMessage(request, "GetTimeRemaining");
    }
}
