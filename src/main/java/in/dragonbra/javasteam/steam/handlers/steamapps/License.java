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

    private final int packageID;

    private final int lastChangeNumber;

    private final Date timeCreated;

    private final Date timeNextProcess;

    private final int minuteLimit;

    private final int minutesUsed;

    private final EPaymentMethod paymentMethod;

    private final EnumSet<ELicenseFlags> licenseFlags;

    private final String purchaseCode;

    private final ELicenseType licenseType;

    private final int territoryCode;

    private final long accessToken;

    private final int ownerAccountID;

    private final int masterPackageID;

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
        accessToken = license.getAccessToken();
        ownerAccountID = license.getOwnerId();
        masterPackageID = license.getMasterPackageId();
    }

    /**
     * @return the package ID used to identify the license.
     */
    public int getPackageID() {
        return packageID;
    }

    /**
     * @return the last change number for this license.
     */
    public int getLastChangeNumber() {
        return lastChangeNumber;
    }

    /**
     * @return the time the license was created.
     */
    public Date getTimeCreated() {
        return timeCreated;
    }

    /**
     * @return the next process time for the license.
     */
    public Date getTimeNextProcess() {
        return timeNextProcess;
    }

    /**
     * @return the minute limit of the license.
     */
    public int getMinuteLimit() {
        return minuteLimit;
    }

    /**
     * @return the minutes used of the license.
     */
    public int getMinutesUsed() {
        return minutesUsed;
    }

    /**
     * @return the payment method used when the license was created.
     */
    public EPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * @return the license flags.
     */
    public EnumSet<ELicenseFlags> getLicenseFlags() {
        return licenseFlags;
    }

    /**
     * @return the two-letter country code where the license was purchased.
     */
    public String getPurchaseCode() {
        return purchaseCode;
    }

    /**
     * @return the type of the license.
     */
    public ELicenseType getLicenseType() {
        return licenseType;
    }

    /**
     * @return the territory code of the license.
     */
    public int getTerritoryCode() {
        return territoryCode;
    }

    /**
     * @return the PICS access token for this package.
     */
    public long getAccessToken() {
        return accessToken;
    }

    /**
     * @return the owner account id of the license.
     */
    public int getOwnerAccountID() {
        return ownerAccountID;
    }

    /**
     * @return the master package id.
     */
    public int getMasterPackageID() {
        return masterPackageID;
    }
}
