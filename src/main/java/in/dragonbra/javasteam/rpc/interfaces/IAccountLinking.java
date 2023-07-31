package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUseraccountSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-05-11
 */
@SuppressWarnings("unused")
public interface IAccountLinking {

    /* CAccountLinking_GetLinkedAccountInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetLinkedAccountInfo(CAccountLinking_GetLinkedAccountInfo_Request request);
}
