package `in`.dragonbra.javasteam.steam.handlers.steamscreenshots

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMAddScreenshot
import `in`.dragonbra.javasteam.steam.handlers.steamscreenshots.callback.ScreenshotAddedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle

/**
 * This handler is used for screenshots.
 */
@Suppress("unused")
class SteamScreenshots : ClientMsgHandler() {

    /**
     * Adds a screenshot to the user's screenshot library. The screenshot image and thumbnail must already exist on the UFS.
     * Results are returned in a [ScreenshotAddedCallback].
     * The returned [in.dragonbra.javasteam.types.AsyncJob] can also be awaited to retrieve the callback result.
     *
     * @param details The details of the screenshot.
     * @return The Job ID of the request. This can be used to find the appropriate [ScreenshotAddedCallback].
     */
    fun addScreenshot(details: ScreenshotDetails): AsyncJobSingle<ScreenshotAddedCallback> {
        val msg = ClientMsgProtobuf<CMsgClientUCMAddScreenshot.Builder>(
            CMsgClientUCMAddScreenshot::class.java,
            EMsg.ClientUCMAddScreenshot
        )
        msg.setSourceJobID(client.getNextJobID())

        details.gameID?.let {
            msg.body.setAppid(it.appID)
        }

        msg.body.setCaption(details.caption)
        msg.body.setFilename(details.ufsImageFilePath)
        msg.body.setPermissions(details.privacy.code())
        msg.body.setThumbname(details.usfThumbnailFilePath)
        msg.body.setWidth(details.width)
        msg.body.setHeight(details.height)
        msg.body.setRtime32Created((details.creationTime.time / 1000L).toInt())

        client.send(msg)

        return AsyncJobSingle(this.client, msg.sourceJobID)
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
        /**
         * Width of a screenshot thumbnail
         */
        const val SCREENSHOT_THUMBNAIL_WIDTH: Int = 200

        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientUCMAddScreenshotResponse -> ScreenshotAddedCallback(packetMsg)
            else -> null
        }
    }
}
