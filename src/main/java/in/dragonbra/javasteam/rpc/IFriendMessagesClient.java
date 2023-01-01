package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.NoResponse;

public interface IFriendMessagesClient {
    NoResponse IncomingMessage(CFriendMessages_IncomingMessage_Notification request);

    NoResponse NotifyAckMessageEcho(CFriendMessages_AckMessage_Notification request);

    NoResponse MessageReaction(CFriendMessages_MessageReaction_Notification request);
}
