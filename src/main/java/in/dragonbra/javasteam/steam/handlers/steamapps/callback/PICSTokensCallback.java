package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSAccessTokenResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This callback is fired when the PICS returns access tokens for a list of appids and packageids
 */
public class PICSTokensCallback extends CallbackMsg {

    private final List<Integer> packageTokensDenied;

    private final List<Integer> appTokensDenied;

    private final Map<Integer, Long> packageTokens;

    private final Map<Integer, Long> appTokens;

    public PICSTokensCallback(JobID jobID, CMsgClientPICSAccessTokenResponse.Builder msg) {
        setJobID(jobID);

        packageTokensDenied = Collections.unmodifiableList(msg.getPackageDeniedTokensList());
        appTokensDenied = Collections.unmodifiableList(msg.getAppDeniedTokensList());

        packageTokens = new HashMap<>();
        appTokens = new HashMap<>();

        for (CMsgClientPICSAccessTokenResponse.PackageToken packageToken : msg.getPackageAccessTokensList()) {
            packageTokens.put(packageToken.getPackageid(), packageToken.getAccessToken());
        }

        for (CMsgClientPICSAccessTokenResponse.AppToken appToken : msg.getAppAccessTokensList()) {
            appTokens.put(appToken.getAppid(), appToken.getAccessToken());
        }
    }

    /**
     * @return a list of denied package tokens.
     */
    public List<Integer> getPackageTokensDenied() {
        return packageTokensDenied;
    }

    /**
     * @return a list of denied app tokens.
     */
    public List<Integer> getAppTokensDenied() {
        return appTokensDenied;
    }

    /**
     * @return a map containing requested package tokens.
     */
    public Map<Integer, Long> getPackageTokens() {
        return packageTokens;
    }

    /**
     * @return a map containing requested package tokens.
     */
    public Map<Integer, Long> getAppTokens() {
        return appTokens;
    }
}
