package `in`.dragonbra.javasteam.steam.handlers.steamworkshop.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMEnumeratePublishedFilesByUserActionResponse
import `in`.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is received in response to calling [SteamWorkshop.enumeratePublishedFilesByUserAction].
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class UserActionPublishedFilesCallback(packetMsg: IPacketMsg?) : CallbackMsg() {

    /**
     * Gets the result.
     */
    val result: EResult

    /**
     * Gets the list of enumerated files.
     */
    val files: List<File>

    /**
     * Gets the count of total results.
     */
    val totalResults: Int

    init {
        val response = ClientMsgProtobuf<CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.Builder>(
            CMsgClientUCMEnumeratePublishedFilesByUserActionResponse::class.java,
            packetMsg
        )
        val msg = response.body

        jobID = response.targetJobID

        result = EResult.from(msg.eresult)

        files = msg.publishedFilesList.map { File(it) }

        totalResults = msg.totalResults
    }

    /**
     * Represents the details of a single published file.
     */
    class File(file: CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.PublishedFileId) {

        /**
         * Gets the file ID.
         */
        val fileID: Long = file.publishedFileId

        /**
         * Gets the timestamp of this file.
         */
        val timestamp: Date = Date(file.rtimeTimeStamp * 1000L)
    }
}
