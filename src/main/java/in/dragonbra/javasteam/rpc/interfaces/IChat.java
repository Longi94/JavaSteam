package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IChat {

    /* CChat_RequestFriendPersonaStates_Response */
    AsyncJobSingle<ServiceMethodResponse> RequestFriendPersonaStates(CChat_RequestFriendPersonaStates_Request request);
}
