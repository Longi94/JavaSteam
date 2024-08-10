package `in`.dragonbra.javasteam.steam.handlers.steamworkshop

import `in`.dragonbra.javasteam.enums.EWorkshopFileAction

/**
 * Represents the details of an enumeration request used for the local user's files.
 *
 * @param appID Gets or Sets the AppID of the workshop to enumerate.
 * @param startIndex Gets or Sets the start index.
 * @param userAction Gets or Sets the user action to filter by. This value is only used by [SteamWorkshop.enumeratePublishedFilesByUserAction]
 */
data class EnumerationUserDetails(
    var appID: Int,
    var startIndex: Int,
    var userAction: EWorkshopFileAction,
)
