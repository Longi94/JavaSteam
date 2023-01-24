package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChatUsability_RequestClientUsabilityMetrics_Notification;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IChatUsabilityClient {

    /* NoResponse */
    void NotifyRequestClientUsabilityMetrics(CChatUsability_RequestClientUsabilityMetrics_Notification request);
}

