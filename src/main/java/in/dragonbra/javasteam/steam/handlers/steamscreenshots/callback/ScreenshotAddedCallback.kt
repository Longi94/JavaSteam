package `in`.dragonbra.javasteam.steam.handlers.steamscreenshots.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMAddScreenshotResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.UGCHandle

/**
 * This callback is fired when a new screenshot is added.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ScreenshotAddedCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result.
     */
    val result: EResult

    /**
     * Gets the screenshot ID of the newly added screenshot.
     */
    val screenshotID: UGCHandle

    init {
        val resp = ClientMsgProtobuf<CMsgClientUCMAddScreenshotResponse.Builder>(
            CMsgClientUCMAddScreenshotResponse::class.java,
            packetMsg
        )
        val msg = resp.body

        jobID = resp.targetJobID

        result = EResult.from(msg.eresult)
        screenshotID = UGCHandle(msg.screenshotid)
    }
}
