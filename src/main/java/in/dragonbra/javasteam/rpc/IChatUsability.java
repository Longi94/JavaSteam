package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient.*;

public interface IChatUsability {
    NoResponse NotifyClientUsabilityMetrics(CChatUsability_ClientUsabilityMetrics_Notification request);
}
