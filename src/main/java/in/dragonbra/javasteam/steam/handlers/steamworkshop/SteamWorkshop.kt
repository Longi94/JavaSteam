package `in`.dragonbra.javasteam.steam.handlers.steamworkshop

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMEnumeratePublishedFilesByUserAction
import `in`.dragonbra.javasteam.steam.handlers.steamworkshop.callback.UserActionPublishedFilesCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle

/**
 * This handler is used for requesting files published on the Steam Workshop.
 */
class SteamWorkshop : ClientMsgHandler() {

    /**
     * Enumerates the list of published files for the current logged-in user based on user action.
     * Results are returned in a [UserActionPublishedFilesCallback].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     *
     * @param details The specific details of the request.
     * @return The Job ID of the request. This can be used to find the appropriate [UserActionPublishedFilesCallback].
     */
    fun enumeratePublishedFilesByUserAction(details: EnumerationUserDetails): AsyncJobSingle<UserActionPublishedFilesCallback> {
        val enumRequest = ClientMsgProtobuf<CMsgClientUCMEnumeratePublishedFilesByUserAction.Builder>(
            CMsgClientUCMEnumeratePublishedFilesByUserAction::class.java,
            EMsg.ClientUCMEnumeratePublishedFilesByUserAction
        )
        enumRequest.setSourceJobID(client.getNextJobID())

        enumRequest.body.setAction(details.userAction.code())
        enumRequest.body.setAppId(details.appID)
        enumRequest.body.setStartIndex(details.startIndex)

        client.send(enumRequest)

        return AsyncJobSingle(this.client, enumRequest.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback: CallbackMsg = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.getMsgType()) {
            EMsg.ClientUCMEnumeratePublishedFilesByUserActionResponse -> UserActionPublishedFilesCallback(packetMsg)
            else -> null
        }
    }
}
