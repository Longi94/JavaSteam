package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.ICloudGaming;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

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
    public JobID CreateNonce(SteammessagesAuthSteamclient.CCloudGaming_CreateNonce_Request request) {
        return sendMessage(request, "CreateNonce");
    }

    @Override
    public JobID GetTimeRemaining(SteammessagesAuthSteamclient.CCloudGaming_GetTimeRemaining_Request request) {
        return sendMessage(request, "GetTimeRemaining");
    }
}
