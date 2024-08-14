package `in`.dragonbra.javasteam.steam.handlers.steamuser

import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.util.Utils

/**
 * Represents the details required to log into Steam3 as an anonymous user.
 *
 * @param cellID Gets or sets the CellID.
 * @param clientOSType Gets or sets the client operating system type.
 * @param clientLanguage Gets or sets the client language.
 */
data class AnonymousLogOnDetails(
    var cellID: Int? = null,
    var clientOSType: EOSType = Utils.getOSType(),
    var clientLanguage: String = "english",
)
