package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChat_RequestFriendPersonaStates_Request;
import in.dragonbra.javasteam.rpc.interfaces.IChat;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class Chat extends UnifiedService implements IChat {

    public Chat(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> requestFriendPersonaStates(CChat_RequestFriendPersonaStates_Request request) {
        return sendMessage(request, "RequestFriendPersonaStates");
    }
}
