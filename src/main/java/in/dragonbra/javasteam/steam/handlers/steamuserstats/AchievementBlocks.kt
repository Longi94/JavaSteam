package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * A Block of achievements with the timestamp of when the achievement (in order of the schema) is unlocked.
 * @param achievementId the achievement id.
 * @param unlockTime a [List] of integers containing when an achievement was unlocked.
 *  An unlockTime of 0 means it has not been achieved, unlocked achievements are displayed as valve-timestamps.
 * @param name the internal name of the achievement (e.g., "ACH_FIRST_BLOOD")
 * @param displayName the localized display name of the achievement
 * @param description the localized description of the achievement
 * @param icon the URL to the achievement's icon
 * @param iconGray the URL to the achievement's grayscale icon (shown when locked)
 * @param hidden whether the achievement is hidden until unlocked
 */
@JavaSteamAddition
data class AchievementBlocks(
    val achievementId: Int,
    val unlockTime: List<Int>,
    val name: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val iconGray: String? = null,
    val hidden: Boolean = false
) {
    /**
     * Returns true if this achievement is unlocked.
     * An achievement is considered unlocked if it has any non-zero unlock time.
     */
    val isUnlocked: Boolean
        get() = unlockTime.any { it > 0 }

    /**
     * Returns the unlock timestamp as a single integer.
     * For expanded achievements with a single timestamp, returns that value.
     * For blocks with multiple timestamps, returns the first non-zero timestamp, or 0 if all locked.
     */
    val unlockTimestamp: Int
        get() = unlockTime.firstOrNull { it > 0 } ?: 0
}
