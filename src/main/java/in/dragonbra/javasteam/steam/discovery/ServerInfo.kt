package `in`.dragonbra.javasteam.steam.discovery

import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import java.time.Instant

/**
 * @author lngtr
 * @since 2018-02-20
 */
class ServerInfo(val record: ServerRecord, val protocol: ProtocolTypes) {
    var lastBadConnectionTimeUtc: Instant? = null
}
