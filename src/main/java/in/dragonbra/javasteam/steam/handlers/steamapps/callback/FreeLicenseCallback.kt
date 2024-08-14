package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestFreeLicenseResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.Collections;
import java.util.List;

/**
 * This callback is received in response to calling {@link SteamApps#requestFreeLicense}, informing the client of newly granted packages, if any.
 */
public class FreeLicenseCallback extends CallbackMsg {

    private final EResult result;

    private final List<Integer> grantedApps;

    private final List<Integer> grantedPackages;

    public FreeLicenseCallback(JobID jobID, CMsgClientRequestFreeLicenseResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());

        grantedApps = Collections.unmodifiableList(msg.getGrantedAppidsList());
        grantedPackages = Collections.unmodifiableList(msg.getGrantedPackageidsList());
    }

    /**
     * @return the result of the message.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the list of granted apps.
     */
    public List<Integer> getGrantedApps() {
        return grantedApps;
    }

    /**
     * @return the list of granted packages.
     */
    public List<Integer> getGrantedPackages() {
        return grantedPackages;
    }
}
