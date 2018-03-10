package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.enums.ELicenseFlags;
import in.dragonbra.javasteam.enums.ELicenseType;
import in.dragonbra.javasteam.enums.EPaymentMethod;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientLicenseList;

import java.util.Date;
import java.util.EnumSet;

/**
 * Represents a granted license (steam3 subscription) for one or more games.
 */
public class License {

    private int packageID;

    private int lastChangeNumber;

    private Date timeCreated;

    private Date timeNextProcess;

    private int minuteLimit;

    private int minutesUsed;

    private EPaymentMethod paymentMethod;

    private EnumSet<ELicenseFlags> licenseFlags;

    private String purchaseCode;

    private ELicenseType licenseType;

    private int territoryCode;

    public License(CMsgClientLicenseList.License license) {
        packageID = license.getPackageId();
        lastChangeNumber = license.getChangeNumber();
        timeCreated = new Date(license.getTimeCreated() * 1000L);
        timeNextProcess = new Date(license.getTimeNextProcess() * 1000L);
        minuteLimit = license.getMinuteLimit();
        minutesUsed = license.getMinutesUsed();
        paymentMethod = EPaymentMethod.from(license.getPaymentMethod());
        licenseFlags = ELicenseFlags.from(license.getFlags());
        purchaseCode = license.getPurchaseCountryCode();
        licenseType = ELicenseType.from(license.getLicenseType());
        territoryCode = license.getTerritoryCode();
    }

    public int getPackageID() {
        return packageID;
    }

    public int getLastChangeNumber() {
        return lastChangeNumber;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public Date getTimeNextProcess() {
        return timeNextProcess;
    }

    public int getMinuteLimit() {
        return minuteLimit;
    }

    public int getMinutesUsed() {
        return minutesUsed;
    }

    public EPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public EnumSet<ELicenseFlags> getLicenseFlags() {
        return licenseFlags;
    }

    public String getPurchaseCode() {
        return purchaseCode;
    }

    public ELicenseType getLicenseType() {
        return licenseType;
    }

    public int getTerritoryCode() {
        return territoryCode;
    }
}
