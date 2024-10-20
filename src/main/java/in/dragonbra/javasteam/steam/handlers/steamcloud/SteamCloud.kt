package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetSingleFileInfo
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetails
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFile
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.ShareFileCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.SingleFileInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.types.UGCHandle

/**
 * This handler is used for interacting with remote storage and user generated content.
 */
class SteamCloud : ClientMsgHandler() {

    /**
     * Requests details for a specific item of user generated content from the Steam servers.
     * Results are returned in a [UGCDetailsCallback].
     *
     * @param ugcId The unique user generated content id.
     * @return The Job ID of the request. This can be used to find the appropriate [UGCDetailsCallback].
     */
    fun requestUGCDetails(ugcId: UGCHandle): AsyncJobSingle<UGCDetailsCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSGetUGCDetails.Builder>(
            CMsgClientUFSGetUGCDetails::class.java,
            EMsg.ClientUFSGetUGCDetails
        ).apply {
            sourceJobID = client.getNextJobID()

            body.hcontent = ugcId.value
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Requests details for a specific file in the user's Cloud storage.
     * Results are returned in a [SingleFileInfoCallback].
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate [SingleFileInfoCallback].
     */
    fun getSingleFileInfo(appId: Int, filename: String): AsyncJobSingle<SingleFileInfoCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSGetSingleFileInfo.Builder>(
            CMsgClientUFSGetSingleFileInfo::class.java,
            EMsg.ClientUFSGetSingleFileInfo
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = appId
            body.fileName = filename
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Commit a Cloud file at the given path to make its UGC handle publicly visible.
     * Results are returned in a [ShareFileCallback].
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate [ShareFileCallback].
     */
    fun shareFile(appId: Int, filename: String): AsyncJobSingle<ShareFileCallback> {
        val request = ClientMsgProtobuf<CMsgClientUFSShareFile.Builder>(
            CMsgClientUFSShareFile::class.java,
            EMsg.ClientUFSShareFile
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = appId
            body.fileName = filename
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
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
            EMsg.ClientUFSGetUGCDetailsResponse -> UGCDetailsCallback(packetMsg)
            EMsg.ClientUFSGetSingleFileInfoResponse -> SingleFileInfoCallback(packetMsg)
            EMsg.ClientUFSShareFileResponse -> ShareFileCallback(packetMsg)
            else -> null
        }
    }
}
