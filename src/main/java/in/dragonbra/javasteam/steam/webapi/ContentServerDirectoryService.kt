package `in`.dragonbra.javasteam.steam.webapi

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Response
import `in`.dragonbra.javasteam.steam.cdn.Server

/**
 * Helper class to load servers from the Content Server Directory Service Web API.
 */
object ContentServerDirectoryService {

    internal fun convertServerList(response: CContentServerDirectory_GetServersForSteamPipe_Response): List<Server> = response.serversList.map { child ->
        val httpsSupport = child.httpsSupport
        val protocol = if (httpsSupport == "mandatory") Server.ConnectionProtocol.HTTPS else Server.ConnectionProtocol.HTTP

        Server(
            protocol = protocol,
            host = child.host,
            vHost = child.vhost,
            port = if (protocol == Server.ConnectionProtocol.HTTPS) 443 else 80,
            type = child.type,
            sourceID = child.sourceId,
            cellID = child.cellId,
            load = child.load,
            weightedLoad = child.weightedLoad,
            numEntries = child.numEntriesInClientList,
            steamChinaOnly = child.steamChinaOnly,
            useAsProxy = child.useAsProxy,
            proxyRequestPathTemplate = child.proxyRequestPathTemplate,
            allowedAppIds = child.allowedAppIdsList.toIntArray()
        )
    }
}
