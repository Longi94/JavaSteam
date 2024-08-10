package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatRoomType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientChatInvite
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired when a chat invite is received.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChatInviteCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the [SteamID] of the user who was invited to the chat.
     */
    val invitedID: SteamID

    /**
     * Gets the chat room [SteamID].
     */
    val chatRoomID: SteamID

    /**
     * Gets the [SteamID] of the user who performed the invitation.
     */
    val patronID: SteamID

    /**
     * Gets the chat room type.
     */
    val chatRoomType: EChatRoomType?

    /**
     * Gets the [SteamID] of the chat friend.
     */
    val friendChatID: SteamID

    /**
     * Gets the name of the chat room.
     */
    val chatRoomName: String

    /**
     * Gets the [GameID] associated with this chat room, if it's a game lobby.
     */
    val gameID: GameID

    init {
        val chatInvite = ClientMsgProtobuf<CMsgClientChatInvite.Builder>(
            CMsgClientChatInvite::class.java,
            packetMsg
        )
        val invite = chatInvite.body

        invitedID = SteamID(invite.steamIdInvited)
        chatRoomID = SteamID(invite.steamIdChat)

        patronID = SteamID(invite.steamIdPatron)

        chatRoomType = EChatRoomType.from(invite.chatroomType)

        friendChatID = SteamID(invite.steamIdFriendChat)

        chatRoomName = invite.chatName
        gameID = GameID(invite.gameId)
    }
}
