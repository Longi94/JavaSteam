package `in`.dragonbra.javasteam.steam.handlers.steamcloud.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetailsResponse
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is received in response to calling [SteamCloud.requestUGCDetails].
 */
@Suppress("MemberVisibilityCanBePrivate")
class UGCDetailsCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult

    /**
     * Gets the App ID the UGC is for.
     */
    val appID: Int

    /**
     * Gets the SteamID of the UGC's creator.
     */
    val creator: SteamID

    /**
     * Gets the URL that the content is located at.
     */
    val url: String

    /**
     * Gets the name of the file.
     */
    val fileName: String

    /**
     * Gets the size of the file.
     */
    val fileSize: Int

    init {
        val infoResponse = ClientMsgProtobuf<CMsgClientUFSGetUGCDetailsResponse.Builder>(
            CMsgClientUFSGetUGCDetailsResponse::class.java,
            packetMsg
        )
        val msg = infoResponse.body

        jobID = infoResponse.targetJobID

        result = EResult.from(msg.eresult)

        appID = msg.appId
        creator = SteamID(msg.steamidCreator)

        url = msg.url

        fileName = msg.filename
        fileSize = msg.fileSize
    }
}
