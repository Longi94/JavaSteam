package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Where Steam stored an image or video that was uploaded to a chat with
 * [SteamCloud.uploadChatMediaToUser] or [SteamCloud.uploadChatMediaToChatRoom].
 */
@JavaSteamAddition
class ChatMediaUploadResult internal constructor(
    /**
     * Gets the UGC ID Steam assigned to the media. It is unsigned, so large IDs read as negative.
     * [SteamCloud.requestUGCDetails] looks the media up again by this ID.
     */
    val ugcID: Long,

    /**
     * Gets the URL the media is located at. This is the address the chat message links to.
     * Empty in case Steam did not report one.
     */
    val url: String,

    /**
     * Gets the name of the file.
     */
    val fileName: String,

    /**
     * Gets the size of the file in bytes.
     */
    val fileSize: Int,

    /**
     * Gets the SHA-1 hash of the file, in lower case hex.
     */
    val fileSHA: String,
) {
    override fun toString(): String =
        "ChatMediaUploadResult(ugcID=${ugcID.toULong()}, url=$url, fileName=$fileName, fileSize=$fileSize, fileSHA=$fileSHA)"
}
