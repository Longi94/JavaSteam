package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUseraccountSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IEmbeddedClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-05-11
 */
@SuppressWarnings("unused")
public class EmbeddedClient extends UnifiedService implements IEmbeddedClient {

    public EmbeddedClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> authorizeCurrentDevice(CEmbeddedClient_AuthorizeCurrentDevice_Request request) {
        return sendMessage(request, "AuthorizeCurrentDevice");
    }
}
