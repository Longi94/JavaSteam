package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.NameTableInstance;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Callback fired in response to calling {@link SteamFriends#requestAliasHistory(SteamID)}.
 */
public class AliasHistoryCallback extends CallbackMsg {

    private List<NameTableInstance> responses;

    public AliasHistoryCallback(JobID jobID, CMsgClientAMGetPersonaNameHistoryResponse.Builder msg) {
        setJobID(jobID);
        responses = new ArrayList<>();

        for (CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance instance : msg.getResponsesList()) {
            responses.add(new NameTableInstance(instance));
        }

        responses = Collections.unmodifiableList(responses);
    }

    /**
     * @return the responses to the steam ids
     */
    public List<NameTableInstance> getResponses() {
        return responses;
    }
}
