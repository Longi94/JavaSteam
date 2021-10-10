package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.PICSChangeData;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.HashMap;
import java.util.Map;

/**
 * This callback is fired when the PICS returns the changes since the last change number
 */
public class PICSChangesCallback extends CallbackMsg {

    private int lastChangeNumber;

    private int currentChangeNumber;

    private boolean requiresFullUpdate;

    private boolean requiresFullAppUpdate;

    private boolean requiresFullPackageUpdate;

    private Map<Integer, PICSChangeData> packageChanges;

    private Map<Integer, PICSChangeData> appChanges;

    public PICSChangesCallback(JobID jobID, CMsgClientPICSChangesSinceResponse.Builder msg) {
        setJobID(jobID);

        lastChangeNumber = msg.getSinceChangeNumber();
        currentChangeNumber = msg.getCurrentChangeNumber();
        requiresFullAppUpdate = msg.getForceFullAppUpdate();
        requiresFullPackageUpdate = msg.getForceFullPackageUpdate();
        requiresFullUpdate = msg.getForceFullUpdate();
        packageChanges = new HashMap<>();
        appChanges = new HashMap<>();

        for (CMsgClientPICSChangesSinceResponse.PackageChange packageChange : msg.getPackageChangesList()) {
            packageChanges.put(packageChange.getPackageid(), new PICSChangeData(packageChange));
        }

        for (CMsgClientPICSChangesSinceResponse.AppChange appChange : msg.getAppChangesList()) {
            appChanges.put(appChange.getAppid(), new PICSChangeData(appChange));
        }
    }

    /**
     * @return the supplied change number for the request.
     */
    public int getLastChangeNumber() {
        return lastChangeNumber;
    }

    /**
     * @return the current change number.
     */
    public int getCurrentChangeNumber() {
        return currentChangeNumber;
    }

    /**
     * @return if this update requires a full update of the information.
     */
    public boolean isRequiresFullUpdate() {
        return requiresFullUpdate;
    }

    /**
     * @return if this update requires a full update of the app information.
     */
    public boolean isRequiresFullPackageUpdate() {
        return requiresFullPackageUpdate;
    }

    /**
     * @return if this update requires a full update of the package information.
     */
    public boolean isRequiresFullAppUpdate() {
        return requiresFullAppUpdate;
    }

    /**
     * @return a map containing requested package tokens.
     */
    public Map<Integer, PICSChangeData> getPackageChanges() {
        return packageChanges;
    }

    /**
     * @return a map containing requested package tokens.
     */
    public Map<Integer, PICSChangeData> getAppChanges() {
        return appChanges;
    }
}
