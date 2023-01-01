package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IChatUsabilityClient {
    NoResponse NotifyRequestClientUsabilityMetrics(CChatUsability_RequestClientUsabilityMetrics_Notification request);
}

