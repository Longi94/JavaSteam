package `in`.dragonbra.javasteam.steam.handlers.steamgameserver

import `in`.dragonbra.javasteam.enums.EServerFlags
import java.net.InetAddress
import java.util.*

/**
 * Represents the details of the game server's current status.
 *
 * @param appID Gets or sets the AppID this game server is serving.
 * @param serverFlags Gets or sets the server's basic state as flags.
 * @param gameDirectory Gets or sets the directory the game data is in.
 * @param address Gets or sets the IP address the game server listens on.
 * @param port Gets or sets the port the game server listens on.
 * @param queryPort Gets or sets the port the game server responds to queries on.
 * @param version Gets or sets the current version of the game server.
 */
class StatusDetails(
    var appID: Int,
    var serverFlags: EnumSet<EServerFlags>,
    var gameDirectory: String?,
    var address: InetAddress?,
    var port: Int,
    var queryPort: Int,
    var version: String?,
)
