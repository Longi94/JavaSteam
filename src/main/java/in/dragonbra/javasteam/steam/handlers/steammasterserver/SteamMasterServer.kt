package `in`.dragonbra.javasteam.steam.handlers.steammasterserver

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgClientGMSServerQuery
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.callback.QueryCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.util.NetHelpers

/**
 * This handler is used for requesting server list details from Steam.
 */
class SteamMasterServer : ClientMsgHandler() {

    /**
     * Requests a list of servers from the Steam game master server.
     * Results are returned in a [QueryCallback].
     *
     * @param details The details for the request.
     * @return The Job ID of the request. This can be used to find the appropriate [QueryCallback].
     */
    fun serverQuery(details: QueryDetails): AsyncJobSingle<QueryCallback> {
        val query = ClientMsgProtobuf<CMsgClientGMSServerQuery.Builder>(
            CMsgClientGMSServerQuery::class.java,
            EMsg.ClientGMSServerQuery
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = details.appID

            if (details.geoLocatedIP != null) {
                body.geoLocationIp = NetHelpers.getIPAddress(details.geoLocatedIP!!)
            }

            body.filterText = details.filter
            body.regionCode = details.region.code().toInt()

            body.maxServers = details.maxServers
        }

        client.send(query)

        return AsyncJobSingle(this.client, query.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.GMSClientServerQueryResponse -> QueryCallback(packetMsg)
            else -> null
        }
    }
}
