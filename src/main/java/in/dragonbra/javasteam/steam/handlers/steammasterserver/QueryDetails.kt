package `in`.dragonbra.javasteam.steam.handlers.steammasterserver

import `in`.dragonbra.javasteam.enums.ERegionCode
import java.net.InetAddress

/**
 * Details used when performing a server list query.
 *
 * @param appID Gets or sets the AppID used when querying servers.
 * @param filter Gets or sets the filter used for querying the master server. Check https://developer.valvesoftware.com/wiki/Master_Server_Query_Protocol for details on how the filter is structured.
 * @param region Gets or sets the region that servers will be returned from.
 * @param geoLocatedIP Gets or sets the IP address that will be GeoIP located. This is done to return servers closer to this location.
 * @param maxServers Gets or sets the maximum number of servers to return.
 */
data class QueryDetails(
    var appID: Int,
    var filter: String?,
    var region: ERegionCode,
    var geoLocatedIP: InetAddress?,
    var maxServers: Int,
)
