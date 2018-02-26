package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgPersonaChangeResponse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is fired in response to setting this client's persona name or state
 * with {@link SteamFriends#setPersonaName(String)} or {@link SteamFriends#setPersonaState(EPersonaState)}.
 */
public class PersonaChangeCallback extends CallbackMsg {
    private EResult result;

    private String name;

    public PersonaChangeCallback(JobID jobID, CMsgPersonaChangeResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getResult());
        name = msg.getPlayerName();
    }

    public EResult getResult() {
        return result;
    }

    public String getName() {
        return name;
    }
}
