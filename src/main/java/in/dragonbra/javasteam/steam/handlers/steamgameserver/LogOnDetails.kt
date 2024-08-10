package `in`.dragonbra.javasteam.steam.handlers.steamgameserver

/**
 * Represents the details required to log into Steam3 as a game server.
 *
 * @param token Gets or sets the authentication token used to log in as a game server.
 * @param appID Gets or sets the AppID this gameserver will serve.
 */
data class LogOnDetails(var token: String?, var appID: Int)
