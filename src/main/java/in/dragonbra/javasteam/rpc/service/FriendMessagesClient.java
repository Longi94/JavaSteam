package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient;
import in.dragonbra.javasteam.rpc.IFriendMessagesClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

// TODO implement

@SuppressWarnings("unused")
public class FriendMessagesClient extends UnifiedService implements IFriendMessagesClient {

    public FriendMessagesClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void IncomingMessage(SteammessagesFriendmessagesSteamclient.CFriendMessages_IncomingMessage_Notification request) {

    }

    @Override
    public void NotifyAckMessageEcho(SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification request) {

    }

    @Override
    public void MessageReaction(SteammessagesFriendmessagesSteamclient.CFriendMessages_MessageReaction_Notification request) {

    }
}
