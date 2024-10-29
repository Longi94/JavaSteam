package `in`.dragonbra.javasteam.steam.webapi

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Response
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Request
import `in`.dragonbra.javasteam.steam.cdn.Server
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import okhttp3.internal.http.HttpMethod

/**
 * Helper class to load servers from the Content Server Directory Service Web API.
 */
object ContentServerDirectoryService {
//    /**
//     * Load a list of servers from the Content Server Directory Service.
//     *
//     * @param configuration Configuration Object
//     * @return A list of [CDN.Server]s.
//     */
//    suspend fun load(configuration: SteamConfiguration): List<Server> =
//        loadCore(configuration, null, null)
//
//    /**
//     * Load a list of servers from the Content Server Directory Service.
//     *
//     * @param configuration Configuration Object
//     * @param cellId Preferred steam cell id
//     * @return A list of [Server]s.
//     */
//    suspend fun load(configuration: SteamConfiguration, cellId: Int): List<Server> =
//        loadCore(configuration, cellId, null)
//
//    /**
//     * Load a list of servers from the Content Server Directory Service.
//     * You can use [SteamContent.getServersForSteamPipe] instead to go over a CM connection.
//     *
//     * @param configuration Configuration Object
//     * @param cellId Preferred steam cell id
//     * @param maxNumServers Max number of servers to return.
//     * @return A list of [Server]s.
//     */
//    suspend fun load(configuration: SteamConfiguration, cellId: Int, maxNumServers: Int): List<Server> =
//        loadCore(configuration, cellId, maxNumServers)
//
//    private suspend fun loadCore(configuration: SteamConfiguration, cellId: Int?, maxNumServers: Int?): List<Server> {
//        requireNotNull(configuration) { "configuration cannot be null" }
//
//        val directory = configuration.getWebAPI("IContentServerDirectoryService")
//        val request = CContentServerDirectory_GetServersForSteamPipe_Request.newBuilder().apply {
//            this.cellId = cellId ?: configuration.cellID
//            maxNumServers?.let { this.maxServers = it }
//        }.build()
//
//        val response = directory.callProtobufAsync<CContentServerDirectory_GetServersForSteamPipe_Response, CContentServerDirectory_GetServersForSteamPipe_Request>(
//            HttpMethod.Get,
//            "GetServersForSteamPipe",
//            request,
//            version = 1
//        )
//
//        return convertServerList(response.body)
//    }
//
    internal fun convertServerList(response: CContentServerDirectory_GetServersForSteamPipe_Response): List<Server> {
        return response.serversList.map { child ->
            val httpsSupport = child.httpsSupport
            val protocol = if (httpsSupport == "mandatory") Server.ConnectionProtocol.HTTPS else Server.ConnectionProtocol.HTTP

            Server(
                protocol = protocol,
                host = child.host,
                vHost = child.vhost,
                port = if (protocol == Server.ConnectionProtocol.HTTPS) 443 else 80,
                type = child.type,
                sourceID = child.sourceId,
                cellID = child.cellId.toInt(),
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
}
