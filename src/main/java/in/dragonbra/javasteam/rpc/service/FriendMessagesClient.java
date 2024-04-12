package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IFriendMessagesClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class FriendMessagesClient extends UnifiedService implements IFriendMessagesClient {

    public FriendMessagesClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void incomingMessage(CFriendMessages_IncomingMessage_Notification request) {
        sendNotification(request, "IncomingMessage");
    }

    @Override
    public void notifyAckMessageEcho(CFriendMessages_AckMessage_Notification request) {
        sendNotification(request, "NotifyAckMessageEcho");
    }

    @Override
    public void messageReaction(CFriendMessages_MessageReaction_Notification request) {
        sendNotification(request, "MessageReaction");
    }
}
