package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

enum class InstalledAppsFilter {
    /**
     * Return everything, including not installed apps
     */
    None,

    /**
     * Return only apps that are "in progress" - downloading, updated, scheduled
     */
    Changing,
}
