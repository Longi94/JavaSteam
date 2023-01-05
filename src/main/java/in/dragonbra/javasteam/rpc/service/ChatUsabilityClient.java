package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient;
import in.dragonbra.javasteam.rpc.IChatUsabilityClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

// TODO implement

@SuppressWarnings("unused")
public class ChatUsabilityClient extends UnifiedService implements IChatUsabilityClient {

    public ChatUsabilityClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyRequestClientUsabilityMetrics(SteammessagesChatSteamclient.CChatUsability_RequestClientUsabilityMetrics_Notification request) {

    }
}
