package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCheckAppBetaPasswordResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * This callback is received when a beta password check has been completed
 */
public class CheckAppBetaPasswordCallback extends CallbackMsg {

    private EResult result;

    private Map<String, byte[]> betaPasswords;

    public CheckAppBetaPasswordCallback(JobID jobID, CMsgClientCheckAppBetaPasswordResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        betaPasswords = new HashMap<>();

        for (CMsgClientCheckAppBetaPasswordResponse.BetaPassword password : msg.getBetapasswordsList()) {
            betaPasswords.put(password.getBetaname(), Strings.decodeHex(password.getBetapassword()));
        }
    }

    public EResult getResult() {
        return result;
    }

    public Map<String, byte[]> getBetaPasswords() {
        return betaPasswords;
    }
}
