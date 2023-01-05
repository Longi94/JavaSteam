package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient;
import in.dragonbra.javasteam.rpc.IChatUsability;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

// TODO implement

@SuppressWarnings("unused")
public class ChatUsability extends UnifiedService implements IChatUsability {

    public ChatUsability(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyClientUsabilityMetrics(SteammessagesChatSteamclient.CChatUsability_ClientUsabilityMetrics_Notification request) {

    }
}
