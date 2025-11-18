package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * A Block of achievements with the timestamp of when the achievement (in order of the schema) is unlocked.
 * @param achievementId the achievement id.
 * @param unlockTime a [List] of integers containing when an achievement was unlocked.
 *  An unlockTime of 0 means it has not been achieved, unlocked achievements are displayed as valve-timestamps.
 * @param name the achievement name (e.g., "First Victory"). Null if not available in schema.
 * @param displayName the achievement display name. Null if not available in schema.
 * @param description the achievement description. Null if not available in schema.
 * @param icon the icon hash for the unlocked (color) version. Null if not available in schema.
 * @param iconGray the icon hash for the locked (grayscale) version. Null if not available in schema.
 * @param hidden whether this achievement is hidden until unlocked.
 */

// TODO: Check whether or not this is actually 1 singular achievement, or a block of multiple achievements.
@JavaSteamAddition
data class AchievementBlocks(
    val achievementId: Int,
    val unlockTime: List<Int>,
    val name: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val iconGray: String? = null,
    val hidden: Boolean = false,
) {
    /**
     * Checks if this achievement is unlocked.
     * @return true if the achievement has been unlocked, false otherwise.
     */
    val isUnlocked: Boolean
        get() = unlockTime.isNotEmpty() && unlockTime[0] != 0

    /**
     * Gets the unlock timestamp for this achievement.
     * @return the Unix timestamp when the achievement was unlocked, or 0 if locked.
     */
    val unlockTimestamp: Int
        get() = if (unlockTime.isNotEmpty()) unlockTime[0] else 0
}
