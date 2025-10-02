package `in`.dragonbra.javasteam.steam.handlers.steamuserstats

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * A Class representing stat values of a game.
 *  This data is commonly used for richer stats in games that support it. For example: Left 4 Dead 2.
 * @param statId The id of the stat. This is used to reference the id in the schema.
 * @param statValue The value of the stat.
 */
@JavaSteamAddition
data class Stats(val statId: Int, val statValue: Int)
