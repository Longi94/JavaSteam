package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesFriendmessagesSteamclient;
import in.dragonbra.javasteam.rpc.IFriendMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement

@SuppressWarnings("unused")
public class FriendMessages extends UnifiedService implements IFriendMessages {

    public FriendMessages(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetRecentMessages(SteammessagesFriendmessagesSteamclient.CFriendMessages_GetRecentMessages_Request request) {
        return null;
    }

    @Override
    public SteammessagesFriendmessagesSteamclient.CFriendsMessages_GetActiveMessageSessions_Response GetActiveMessageSessions(SteammessagesFriendmessagesSteamclient.CFriendsMessages_GetActiveMessageSessions_Request request) {
        return null;
    }

    @Override
    public JobID SendMessage(SteammessagesFriendmessagesSteamclient.CFriendMessages_SendMessage_Request request) {
        return null;
    }

    @Override
    public void AckMessage(SteammessagesFriendmessagesSteamclient.CFriendMessages_AckMessage_Notification request) {

    }

    @Override
    public JobID IsInFriendsUIBeta(SteammessagesFriendmessagesSteamclient.CFriendMessages_IsInFriendsUIBeta_Request request) {
        return null;
    }

    @Override
    public JobID UpdateMessageReaction(SteammessagesFriendmessagesSteamclient.CFriendMessages_UpdateMessageReaction_Request request) {
        return null;
    }
}
