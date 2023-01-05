package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChatUsability_ClientUsabilityMetrics_Notification;

/**
 * @author Lossy
 * @since 2023-01-04
 */
public interface IChatUsability {

    /* NoResponse */
    void NotifyClientUsabilityMetrics(CChatUsability_ClientUsabilityMetrics_Notification request);
}
