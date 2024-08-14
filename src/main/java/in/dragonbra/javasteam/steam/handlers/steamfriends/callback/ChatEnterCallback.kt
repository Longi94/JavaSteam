package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatRoomEnterResponse
import `in`.dragonbra.javasteam.enums.EChatRoomType
import `in`.dragonbra.javasteam.generated.MsgClientChatEnter
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.ChatMemberInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * This callback is fired in response to attempting to join a chat.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChatEnterCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the [SteamID] of the chat room.
     */
    val chatID: SteamID

    /**
     * Gets the friend ID.
     */
    val friendID: SteamID

    /**
     * Gets the type of the chat room.
     */
    val chatRoomType: EChatRoomType

    /**
     * Gets the [SteamID] of the chat room owner.
     */
    val ownerID: SteamID

    /**
     * Gets the clan [SteamID] that owns this chat room.
     */
    val clanID: SteamID

    /**
     * Gets the chat flags.
     */
    val chatFlags: Byte

    /**
     * Gets the chat enter response.
     */
    val enterResponse: EChatRoomEnterResponse

    /**
     * Gets the number of users currently in this chat room.
     */
    val numChatMembers: Int

    /**
     * Gets the name of the chat room.
     */
    var chatRoomName: String = ""
        private set

    /**
     * Gets a list of [ChatMemberInfo] instances for each of the members of this chat room.
     */
    var chatMembers: List<ChatMemberInfo> = listOf()
        private set

    init {
        val chatEnter = ClientMsg(MsgClientChatEnter::class.java, packetMsg)
        val msg = chatEnter.body

        chatID = msg.steamIdChat
        friendID = msg.steamIdFriend

        chatRoomType = msg.chatRoomType

        ownerID = msg.steamIdOwner
        clanID = msg.steamIdClan

        chatFlags = msg.chatFlags

        enterResponse = msg.enterResponse

        numChatMembers = msg.numMembers

        val bais = ByteArrayInputStream(chatEnter.payload.toByteArray())

        try {
            BinaryReader(bais).use { br ->
                // steamclient always attempts to read the chat room name, regardless of the enter response
                chatRoomName = br.readNullTermString(StandardCharsets.UTF_8)

                if (enterResponse != EChatRoomEnterResponse.Success) {
                    // the rest of the payload depends on a successful chat enter
                    return@use
                }

                val memberList: MutableList<ChatMemberInfo> = ArrayList<ChatMemberInfo>()

                for (i in 0 until numChatMembers) {
                    val memberInfo = ChatMemberInfo()
                    memberInfo.readFromStream(br)

                    memberList.add(memberInfo)
                }
                chatMembers = Collections.unmodifiableList(memberList)
            }
        } catch (ignored: IOException) {
        }
    }
}
