package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChatUsability_RequestClientUsabilityMetrics_Notification;
import in.dragonbra.javasteam.rpc.interfaces.IChatUsabilityClient;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ChatUsabilityClient extends UnifiedService implements IChatUsabilityClient {

    public ChatUsabilityClient(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void NotifyRequestClientUsabilityMetrics(CChatUsability_RequestClientUsabilityMetrics_Notification request) {
        sendNotification(request, "NotifyRequestClientUsabilityMetrics");
    }
}
