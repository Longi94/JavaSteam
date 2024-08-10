package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatInfoType
import `in`.dragonbra.javasteam.generated.MsgClientChatRoomInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired in response to chat room info being received.
 */
class ChatRoomInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets [SteamID] of the chat room.
     */
    val chatRoomID: SteamID

    /**
     * Gets the info type.
     */
    val type: EChatInfoType

    init {
        val roomInfo = ClientMsg(MsgClientChatRoomInfo::class.java, packetMsg)
        val msg = roomInfo.body

        chatRoomID = msg.steamIdChat
        type = msg.type

        // todo: handle inner payload based on the type similar to ChatMemberInfoCallback
    }
}
