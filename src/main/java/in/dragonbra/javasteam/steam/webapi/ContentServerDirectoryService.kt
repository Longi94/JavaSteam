package `in`.dragonbra.javasteam.steam.webapi

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Response
import `in`.dragonbra.javasteam.steam.cdn.Server

/**
 * Helper class to load servers from the Content Server Directory Service Web API.
 */
object ContentServerDirectoryService {

    @JvmStatic
    internal fun convertServerList(
        response: CContentServerDirectory_GetServersForSteamPipe_Response,
    ): List<Server> = response.serversList.map { child ->
        val httpsSupport = child.httpsSupport
        val protocol = if (httpsSupport == "mandatory") {
            Server.ConnectionProtocol.HTTPS
        } else {
            Server.ConnectionProtocol.HTTP
        }

        Server().apply {
            this.protocol = protocol
            this.host = child.host
            this.vHost = child.vhost
            this.port = if (protocol == Server.ConnectionProtocol.HTTPS) 443 else 80
            this.type = child.type
            this.sourceId = child.sourceId
            this.cellId = child.cellId
            this.load = child.load
            this.weightedLoad = child.weightedLoad
            this.numEntries = child.numEntriesInClientList
            this.steamChinaOnly = child.steamChinaOnly
            this.useAsProxy = child.useAsProxy
            this.proxyRequestPathTemplate = child.proxyRequestPathTemplate
            this.allowedAppIds = child.allowedAppIdsList.toIntArray()
        }
    }
}
