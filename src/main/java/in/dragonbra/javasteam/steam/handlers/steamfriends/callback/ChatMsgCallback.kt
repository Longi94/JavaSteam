package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.generated.MsgClientChatMsg
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.nio.charset.StandardCharsets

/**
 * This callback is fired when a chat room message arrives.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChatMsgCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the [SteamID] of the chatter.
     */
    val chatterID: SteamID

    /**
     * Gets the [SteamID] of the chat room.
     */
    val chatRoomID: SteamID

    /**
     * Gets chat entry type.
     */
    val chatMsgType: EChatEntryType

    /**
     * Gets the message.
     */
    val message: String

    init {
        val chatMsg = ClientMsg(MsgClientChatMsg::class.java, packetMsg)
        val msg = chatMsg.body

        chatterID = msg.steamIdChatter
        chatRoomID = msg.steamIdChatRoom

        chatMsgType = msg.chatMsgType

        // trim any extra null chars from the end
        val payload = chatMsg.payload
        message = String(payload.toByteArray(), StandardCharsets.UTF_8).replace("\u0000+$".toRegex(), "")
    }
}
