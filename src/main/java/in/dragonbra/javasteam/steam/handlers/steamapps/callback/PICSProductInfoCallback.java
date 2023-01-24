package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.PICSProductInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This callback is fired when the PICS returns the product information requested
 */
public class PICSProductInfoCallback extends CallbackMsg {

    private final boolean metaDataOnly;

    private final boolean responsePending;

    private final List<Integer> unknownPackages;

    private final List<Integer> unknownApps;

    private final Map<Integer, PICSProductInfo> apps;

    private final Map<Integer, PICSProductInfo> packages;

    public PICSProductInfoCallback(JobID jobID, CMsgClientPICSProductInfoResponse.Builder msg) {
        setJobID(jobID);

        metaDataOnly = msg.getMetaDataOnly();
        responsePending = msg.getResponsePending();
        unknownPackages = Collections.unmodifiableList(msg.getUnknownPackageidsList());
        unknownApps = Collections.unmodifiableList(msg.getUnknownAppidsList());
        apps = new HashMap<>();
        packages = new HashMap<>();

        for (CMsgClientPICSProductInfoResponse.PackageInfo packageInfo : msg.getPackagesList()) {
            packages.put(packageInfo.getPackageid(), new PICSProductInfo(packageInfo));
        }

        for (CMsgClientPICSProductInfoResponse.AppInfo appInfo : msg.getAppsList()) {
            apps.put(appInfo.getAppid(), new PICSProductInfo(msg, appInfo));
        }
    }

    /**
     * @return if this response contains only product metadata.
     */
    public boolean isMetaDataOnly() {
        return metaDataOnly;
    }

    /**
     * @return if there are more product information responses pending.
     */
    public boolean isResponsePending() {
        return responsePending;
    }

    /**
     * @return a list of unknown package ids.
     */
    public List<Integer> getUnknownPackages() {
        return unknownPackages;
    }

    /**
     * @return a list of unknown app ids.
     */
    public List<Integer> getUnknownApps() {
        return unknownApps;
    }

    /**
     * @return a map containing requested app info.
     */
    public Map<Integer, PICSProductInfo> getApps() {
        return apps;
    }

    /**
     * @return a map containing requested package info.
     */
    public Map<Integer, PICSProductInfo> getPackages() {
        return packages;
    }
}
