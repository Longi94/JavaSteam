package `in`.dragonbra.javasteam.steam.handlers.steamapps

import `in`.dragonbra.javasteam.types.KeyValue

/**
 * Represents Steam store categories/features for an app.
 * Maps the category flags from PICS data to readable properties.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class SteamAppCategories(categoryKv: KeyValue?) {

    /**
     * Multi-player support
     */
    val hasMultiplayer: Boolean = hasCategory(categoryKv, 1)

    /**
     * Single-player support
     */
    val hasSinglePlayer: Boolean = hasCategory(categoryKv, 2)

    /**
     * Valve Anti-Cheat enabled
     */
    val hasValveAntiCheat: Boolean = hasCategory(categoryKv, 8)

    /**
     * Co-op support
     */
    val hasCoop: Boolean = hasCategory(categoryKv, 9)

    /**
     * HDR available
     */
    val hasHDR: Boolean = hasCategory(categoryKv, 12)

    /**
     * Stats tracking
     */
    val hasStats: Boolean = hasCategory(categoryKv, 15)

    /**
     * Includes level editor
     */
    val hasLevelEditor: Boolean = hasCategory(categoryKv, 17)

    /**
     * Partial Controller Support
     */
    val hasPartialControllerSupport: Boolean = hasCategory(categoryKv, 18)

    /**
     * Mods available (Workshop or other)
     */
    val hasMods: Boolean = hasCategory(categoryKv, 19)

    /**
     * MMO game
     */
    val isMMO: Boolean = hasCategory(categoryKv, 20)

    /**
     * Steam Achievements
     */
    val hasSteamAchievements: Boolean = hasCategory(categoryKv, 22)

    /**
     * Steam Cloud saves
     */
    val hasSteamCloud: Boolean = hasCategory(categoryKv, 23)

    /**
     * Shared/Split Screen support
     */
    val hasSharedSplitScreen: Boolean = hasCategory(categoryKv, 24)

    /**
     * Steam Leaderboards
     */
    val hasSteamLeaderboards: Boolean = hasCategory(categoryKv, 25)

    /**
     * Cross-Platform Multiplayer
     */
    val hasCrossPlatformMultiplayer: Boolean = hasCategory(categoryKv, 27)

    /**
     * Full Controller Support
     */
    val hasFullControllerSupport: Boolean = hasCategory(categoryKv, 28)

    /**
     * Steam Trading Cards
     */
    val hasSteamTradingCards: Boolean = hasCategory(categoryKv, 29)

    /**
     * Steam Workshop
     */
    val hasSteamWorkshop: Boolean = hasCategory(categoryKv, 30)

    /**
     * VR Support
     */
    val hasVRSupport: Boolean = hasCategory(categoryKv, 31)

    /**
     * Steam Turn Notifications
     */
    val hasSteamTurnNotifications: Boolean = hasCategory(categoryKv, 32)

    /**
     * In-App Purchases
     */
    val hasInAppPurchases: Boolean = hasCategory(categoryKv, 35)

    /**
     * Online PvP
     */
    val hasOnlinePvP: Boolean = hasCategory(categoryKv, 36)

    /**
     * Local PvP (Shared/Split Screen PvP)
     */
    val hasLocalPvP: Boolean = hasCategory(categoryKv, 37)

    /**
     * Online Co-op
     */
    val hasOnlineCoop: Boolean = hasCategory(categoryKv, 38)

    /**
     * Local Co-op (Shared/Split Screen Co-op)
     */
    val hasLocalCoop: Boolean = hasCategory(categoryKv, 39)

    /**
     * Remote Play on Phone
     */
    val hasRemotePlayOnPhone: Boolean = hasCategory(categoryKv, 41)

    /**
     * Remote Play on Tablet
     */
    val hasRemotePlayOnTablet: Boolean = hasCategory(categoryKv, 42)

    /**
     * Remote Play on TV
     */
    val hasRemotePlayOnTV: Boolean = hasCategory(categoryKv, 43)

    /**
     * Remote Play Together
     */
    val hasRemotePlayTogether: Boolean = hasCategory(categoryKv, 44)

    /**
     * Remote Play Together (alternative category)
     */
    val hasRemotePlayTogether2: Boolean = hasCategory(categoryKv, 45)

    /**
     * Family Sharing
     */
    val hasFamilySharing: Boolean = hasCategory(categoryKv, 46)

    /**
     * Cloud Gaming (NVIDIA GeForce NOW)
     */
    val hasCloudGaming: Boolean = hasCategory(categoryKv, 47)

    /**
     * LAN PvP
     */
    val hasLANPvP: Boolean = hasCategory(categoryKv, 48)

    /**
     * LAN Co-op
     */
    val hasLANCoop: Boolean = hasCategory(categoryKv, 49)

    /**
     * PvP
     */
    val hasPvP: Boolean = hasCategory(categoryKv, 50)

    /**
     * VR Only
     */
    val isVROnly: Boolean = hasCategory(categoryKv, 52)

    /**
     * Captions available
     */
    val hasCaptions: Boolean = hasCategory(categoryKv, 53)

    /**
     * Commentary available
     */
    val hasCommentary: Boolean = hasCategory(categoryKv, 54)

    /**
     * Steam Timeline
     */
    val hasSteamTimeline: Boolean = hasCategory(categoryKv, 55)

    /**
     * VR Supported (not VR-only)
     */
    val hasVRSupported: Boolean = hasCategory(categoryKv, 62)

    /**
     * Gets a list of all enabled category names
     */
    fun getEnabledCategories(): List<String> {
        val categories = mutableListOf<String>()

        if (hasSinglePlayer) categories.add("Single-player")
        if (hasMultiplayer) categories.add("Multi-player")
        if (hasCoop) categories.add("Co-op")
        if (hasOnlineCoop) categories.add("Online Co-op")
        if (hasLocalCoop) categories.add("Local Co-op")
        if (hasLANCoop) categories.add("LAN Co-op")
        if (hasPvP) categories.add("PvP")
        if (hasOnlinePvP) categories.add("Online PvP")
        if (hasLocalPvP) categories.add("Local PvP")
        if (hasLANPvP) categories.add("LAN PvP")
        if (hasCrossPlatformMultiplayer) categories.add("Cross-Platform Multiplayer")
        if (hasSharedSplitScreen) categories.add("Shared/Split Screen")

        if (hasFullControllerSupport) {
            categories.add("Full Controller Support")
        } else if (hasPartialControllerSupport) {
            categories.add("Partial Controller Support")
        }

        if (hasSteamAchievements) categories.add("Steam Achievements")
        if (hasSteamCloud) categories.add("Steam Cloud")
        if (hasSteamLeaderboards) categories.add("Steam Leaderboards")
        if (hasSteamTradingCards) categories.add("Steam Trading Cards")
        if (hasSteamWorkshop) categories.add("Steam Workshop")
        if (hasSteamTurnNotifications) categories.add("Steam Turn Notifications")
        if (hasSteamTimeline) categories.add("Steam Timeline")

        if (isVROnly) {
            categories.add("VR Only")
        } else if (hasVRSupport || hasVRSupported) {
            categories.add("VR Supported")
        }

        if (hasRemotePlayOnPhone) categories.add("Remote Play on Phone")
        if (hasRemotePlayOnTablet) categories.add("Remote Play on Tablet")
        if (hasRemotePlayOnTV) categories.add("Remote Play on TV")
        if (hasRemotePlayTogether || hasRemotePlayTogether2) categories.add("Remote Play Together")

        if (hasFamilySharing) categories.add("Family Sharing")
        if (hasCloudGaming) categories.add("Cloud Gaming")

        if (hasStats) categories.add("Stats")
        if (hasMods) categories.add("Mods")
        if (hasLevelEditor) categories.add("Level Editor")
        if (hasInAppPurchases) categories.add("In-App Purchases")
        if (hasValveAntiCheat) categories.add("Valve Anti-Cheat")
        if (isMMO) categories.add("MMO")
        if (hasHDR) categories.add("HDR")
        if (hasCaptions) categories.add("Captions")
        if (hasCommentary) categories.add("Commentary")

        return categories
    }

    /**
     * Gets a map of all category IDs to their status
     */
    fun getAllCategories(): Map<Int, Boolean> = mapOf(
        1 to hasMultiplayer,
        2 to hasSinglePlayer,
        8 to hasValveAntiCheat,
        9 to hasCoop,
        12 to hasHDR,
        15 to hasStats,
        17 to hasLevelEditor,
        18 to hasPartialControllerSupport,
        19 to hasMods,
        20 to isMMO,
        22 to hasSteamAchievements,
        23 to hasSteamCloud,
        24 to hasSharedSplitScreen,
        25 to hasSteamLeaderboards,
        27 to hasCrossPlatformMultiplayer,
        28 to hasFullControllerSupport,
        29 to hasSteamTradingCards,
        30 to hasSteamWorkshop,
        31 to hasVRSupport,
        32 to hasSteamTurnNotifications,
        35 to hasInAppPurchases,
        36 to hasOnlinePvP,
        37 to hasLocalPvP,
        38 to hasOnlineCoop,
        39 to hasLocalCoop,
        41 to hasRemotePlayOnPhone,
        42 to hasRemotePlayOnTablet,
        43 to hasRemotePlayOnTV,
        44 to hasRemotePlayTogether,
        45 to hasRemotePlayTogether2,
        46 to hasFamilySharing,
        47 to hasCloudGaming,
        48 to hasLANPvP,
        49 to hasLANCoop,
        50 to hasPvP,
        52 to isVROnly,
        53 to hasCaptions,
        54 to hasCommentary,
        55 to hasSteamTimeline,
        62 to hasVRSupported
    )

    companion object {
        /**
         * Checks if a specific category ID is present in the KeyValue data
         */
        private fun hasCategory(categoryKv: KeyValue?, categoryId: Int): Boolean {
            if (categoryKv == null) return false
            val categoryKey = "category_$categoryId"
            val value = categoryKv[categoryKey]?.asInteger()
            return value == 1
        }

        /**
         * Gets the category name for a given category ID
         */
        @JvmStatic
        fun getCategoryName(categoryId: Int): String? = when (categoryId) {
            1 -> "Multi-player"
            2 -> "Single-player"
            8 -> "Valve Anti-Cheat"
            9 -> "Co-op"
            12 -> "HDR"
            15 -> "Stats"
            17 -> "Level Editor"
            18 -> "Partial Controller Support"
            19 -> "Mods"
            20 -> "MMO"
            22 -> "Steam Achievements"
            23 -> "Steam Cloud"
            24 -> "Shared/Split Screen"
            25 -> "Steam Leaderboards"
            27 -> "Cross-Platform Multiplayer"
            28 -> "Full Controller Support"
            29 -> "Steam Trading Cards"
            30 -> "Steam Workshop"
            31 -> "VR Support"
            32 -> "Steam Turn Notifications"
            35 -> "In-App Purchases"
            36 -> "Online PvP"
            37 -> "Local PvP"
            38 -> "Online Co-op"
            39 -> "Local Co-op"
            41 -> "Remote Play on Phone"
            42 -> "Remote Play on Tablet"
            43 -> "Remote Play on TV"
            44 -> "Remote Play Together"
            45 -> "Remote Play Together"
            46 -> "Family Sharing"
            47 -> "Cloud Gaming"
            48 -> "LAN PvP"
            49 -> "LAN Co-op"
            50 -> "PvP"
            52 -> "VR Only"
            53 -> "Captions"
            54 -> "Commentary"
            55 -> "Steam Timeline"
            62 -> "VR Supported"
            else -> null
        }
    }
}
