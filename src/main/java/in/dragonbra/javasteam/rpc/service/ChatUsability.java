package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChatUsability_ClientUsabilityMetrics_Notification;
import in.dragonbra.javasteam.rpc.interfaces.IChatUsability;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ChatUsability extends UnifiedService implements IChatUsability {

    public ChatUsability(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public void notifyClientUsabilityMetrics(CChatUsability_ClientUsabilityMetrics_Notification request) {
        sendNotification(request, "NotifyClientUsabilityMetrics");
    }
}
