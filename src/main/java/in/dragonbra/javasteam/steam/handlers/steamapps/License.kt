package `in`.dragonbra.javasteam.steam.handlers.steamapps

import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientLicenseList
import java.util.*

/**
 * Represents a granted license (steam3 subscription) for one or more games.
 */
@Suppress("unused")
class License(license: CMsgClientLicenseList.License) {

    /**
     * Gets the package ID used to identify the license.
     */
    val packageID: Int = license.packageId

    /**
     * Gets the last change number for this license.
     */
    val lastChangeNumber: Int = license.changeNumber

    /**
     * Gets the time the license was created.
     */
    val timeCreated: Date = Date(license.timeCreated * 1000L)

    /**
     * Gets the next process time for the license.
     */
    val timeNextProcess: Date = Date(license.timeNextProcess * 1000L)

    /**
     * Gets the minute limit of the license.
     */
    val minuteLimit: Int = license.minuteLimit

    /**
     * Gets the minutes used of the license.
     */
    val minutesUsed: Int = license.minutesUsed

    /**
     * Gets the payment method used when the license was created.
     */
    val paymentMethod: EPaymentMethod? = EPaymentMethod.from(license.paymentMethod)

    /**
     * Gets the license flags.
     */
    val licenseFlags: EnumSet<ELicenseFlags> = ELicenseFlags.from(license.flags)

    /**
     * Gets the two-letter country code where the license was purchased.
     */
    val purchaseCode: String = license.purchaseCountryCode

    /**
     * Gets the type of the license.
     */
    val licenseType: ELicenseType? = ELicenseType.from(license.licenseType)

    /**
     * Gets the territory code of the license.
     */
    val territoryCode: Int = license.territoryCode

    /**
     * Gets the PICS access token for this package.
     */
    val accessToken: Long = license.accessToken

    /**
     * Gets the owner account id of the license.
     */
    val ownerAccountID: Int = license.ownerId

    /**
     * Gets the master package id.
     */
    val masterPackageID: Int = license.masterPackageId
}
