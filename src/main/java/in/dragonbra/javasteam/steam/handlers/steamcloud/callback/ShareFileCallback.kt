package `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFileResponse
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamCloud.shareFile].
 */
class ShareFileCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the resulting UGC handle.
     */
    val ugcId: Long

    init {
        val shareResponse = ClientMsgProtobuf<CMsgClientUFSShareFileResponse.Builder>(
            CMsgClientUFSShareFileResponse::class.java,
            packetMsg
        )
        val msg = shareResponse.body

        jobID = shareResponse.targetJobID

        result = EResult.from(msg.eresult)

        ugcId = msg.hcontent
    }
}
