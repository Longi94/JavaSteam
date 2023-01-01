package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;

public interface IChat {
    CChat_RequestFriendPersonaStates_Response RequestFriendPersonaStates(CChat_RequestFriendPersonaStates_Request request);
}
