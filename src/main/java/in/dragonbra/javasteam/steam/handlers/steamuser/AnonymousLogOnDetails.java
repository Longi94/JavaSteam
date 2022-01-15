package in.dragonbra.javasteam.steam.handlers.steamuser;

import in.dragonbra.javasteam.enums.EOSType;
import in.dragonbra.javasteam.util.Utils;

/**
 * Represents the details required to log into Steam3 as an anonymous user.
 */
public class AnonymousLogOnDetails {

    private Integer cellID;

    private EOSType clientOSType;

    private String clientLanguage;

    public AnonymousLogOnDetails() {
        clientOSType = Utils.getOSType();
        clientLanguage = "english";
    }

    /**
     * Gets the CellID
     *
     * @return the CellID.
     */
    public Integer getCellID() {
        return cellID;
    }

    /**
     * Sets the CellID
     *
     * @param cellID the CellID.
     */
    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    /**
     * Gets the client operating system type.
     *
     * @return the client operating system type.
     */
    public EOSType getClientOSType() {
        return clientOSType;
    }

    /**
     * Sets the client operating system type.
     *
     * @param clientOSType the client operating system type.
     */
    public void setClientOSType(EOSType clientOSType) {
        this.clientOSType = clientOSType;
    }

    /**
     * Gets the client language.
     *
     * @return the client language.
     */
    public String getClientLanguage() {
        return clientLanguage;
    }

    /**
     * Sets the client language.
     *
     * @param clientLanguage the client language.
     */
    public void setClientLanguage(String clientLanguage) {
        this.clientLanguage = clientLanguage;
    }
}
