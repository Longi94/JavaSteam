package `in`.dragonbra.javasteam.steam.webapi

import `in`.dragonbra.javasteam.steam.discovery.ServerRecord
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import java.io.IOException

/**
 * Helper class to load servers from the Steam Directory Web API.
 */
object SteamDirectory {

    /**
     * Load a list of servers from the Steam Directory.
     *
     * @param configuration Configuration Object
     * @param maxServers Max number of servers to return. The API will typically return this number per server type (socket and websocket).
     * @return the list of servers
     * @throws IOException if the request could not be executed
     */
    @JvmOverloads
    @JvmStatic
    @Throws(IOException::class)
    fun load(
        configuration: SteamConfiguration,
        maxServers: Int? = null,
    ): List<ServerRecord> = loadCore(configuration, maxServers)

    @Throws(IOException::class)
    private fun loadCore(configuration: SteamConfiguration, maxServers: Int?): List<ServerRecord> {
        val api = configuration.getWebAPI("ISteamDirectory")

        // Even though it doesn't matter the order, we'll still make a specific order.
        val params = hashMapOf<String, String>().apply {
            put("cellid", configuration.cellID.toString())
            maxServers?.let { put("maxcount", it.toString()) }
        }

        val response = api.call("GetCMListForConnect", params)

        if (!response["success"].asBoolean()) {
            throw IOException("Steam Web API returned EResult.${response["message"].asString() ?: "unknown"}")
        }

        val socketList = response["serverlist"]

        val serverRecords = ArrayList<ServerRecord>(socketList.children.size)

        socketList.children.forEach { child ->
            val endPoint = child["endPoint"].value ?: return@forEach

            val record = when (child["type"].value) {
                "websockets" -> ServerRecord.createWebSocketServer(endPoint)
                "netfilter" -> ServerRecord.tryCreateSocketServer(endPoint)
                else -> null
            }

            if (record != null) {
                serverRecords.add(record)
            }
        }

        return serverRecords
    }
}
