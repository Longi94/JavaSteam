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

    public int getLastChangeNumber() {
        return lastChangeNumber;
    }

    public int getCurrentChangeNumber() {
        return currentChangeNumber;
    }

    public boolean isRequiresFullUpdate() {
        return requiresFullUpdate;
    }

    public boolean isRequiresFullPackageUpdate() {
        return requiresFullPackageUpdate;
    }

    public boolean isRequiresFullAppUpdate() {
        return requiresFullAppUpdate;
    }

    public Map<Integer, PICSChangeData> getPackageChanges() {
        return packageChanges;
    }

    public Map<Integer, PICSChangeData> getAppChanges() {
        return appChanges;
    }
}
