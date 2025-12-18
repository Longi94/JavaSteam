package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.util.JavaSteamAddition

@JavaSteamAddition
enum class InstalledAppsFilter {
    /**
     * Return everything, including not installed apps
     */
    None,

    /**
     * Return only apps that are "in progress" - downloading, updating, scheduled
     */
    Changing,
}
