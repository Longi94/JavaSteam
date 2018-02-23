package in.dragonbra.javasteam.steam.handlers.steamuser;

import in.dragonbra.javasteam.enums.EOSType;
import in.dragonbra.javasteam.util.Utils;

/**
 * Represents the details required to log into Steam3 as an anonymous user.
 */
public class AnonymousLogOnDetails {

    private int cellID;

    private EOSType clientOSType;

    private String clientLanguage;

    public AnonymousLogOnDetails() {
        clientOSType = Utils.getOSType();
        clientLanguage = "english";
    }

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public EOSType getClientOSType() {
        return clientOSType;
    }

    public void setClientOSType(EOSType clientOSType) {
        this.clientOSType = clientOSType;
    }

    public String getClientLanguage() {
        return clientLanguage;
    }

    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }
}
