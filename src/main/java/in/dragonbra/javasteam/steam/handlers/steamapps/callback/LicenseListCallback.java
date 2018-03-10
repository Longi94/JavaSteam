package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientLicenseList;
import in.dragonbra.javasteam.steam.handlers.steamapps.License;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is fired during logon, informing the client of it's available licenses.
 */
public class LicenseListCallback extends CallbackMsg {

    private EResult result;

    private List<License> licenseList;

    public LicenseListCallback(CMsgClientLicenseList.Builder msg) {
        result = EResult.from(msg.getEresult());

        List<License> licenses = new ArrayList<>();

        for (CMsgClientLicenseList.License l : msg.getLicensesList()) {
            licenses.add(new License(l));
        }

        licenseList = Collections.unmodifiableList(licenses);
    }

    public EResult getResult() {
        return result;
    }

    public List<License> getLicenseList() {
        return licenseList;
    }
}
