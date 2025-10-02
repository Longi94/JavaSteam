package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * A Block of achievements with the timestamp of when the achievement (in order of the schema) is unlocked.
 * @param achievementId the achievement id.
 * @param unlockTime a [List] of integers containing when an achievement was unlocked.
 *  An unlockTime of 0 means it has not been achieved, unlocked achievements are displayed as valve-timestamps.
 */
@JavaSteamAddition
data class AchievementBlocks(val achievementId: Int, val unlockTime: List<Int>)
