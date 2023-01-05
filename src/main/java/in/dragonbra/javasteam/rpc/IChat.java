package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IChat {

    /* CChat_RequestFriendPersonaStates_Response */
    JobID RequestFriendPersonaStates(CChat_RequestFriendPersonaStates_Request request);
}
