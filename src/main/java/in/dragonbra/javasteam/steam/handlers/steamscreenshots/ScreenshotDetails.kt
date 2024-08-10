package `in`.dragonbra.javasteam.steam.handlers.steamscreenshots

import `in`.dragonbra.javasteam.enums.EUCMFilePrivacyState
import `in`.dragonbra.javasteam.types.GameID
import java.util.*

/**
 * Represents the details required to add a screenshot
 *
 * @param gameID Gets or sets the Steam game ID this screenshot belongs to
 * @param ufsImageFilePath Gets or sets the UFS image filepath.
 * @param usfThumbnailFilePath Gets or sets the UFS thumbnail filepath.
 * @param caption Gets or sets the screenshot caption
 * @param privacy Gets or sets the screenshot privacy
 * @param width Gets or sets the screenshot width
 * @param height Gets or sets the screenshot height
 * @param creationTime Gets or sets the creation time
 * @param isContainsSpoilers Gets or sets whether the screenshot contains spoilers
 */
data class ScreenshotDetails(
    var gameID: GameID?,
    var ufsImageFilePath: String?,
    var usfThumbnailFilePath: String?,
    var caption: String?,
    var privacy: EUCMFilePrivacyState,
    var width: Int,
    var height: Int,
    var creationTime: Date,
    var isContainsSpoilers: Boolean,
)
