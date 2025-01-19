package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendMsgIncoming
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.nio.charset.StandardCharsets

/**
 * This callback is fired in response to receiving a message from a friend.
 */
@Suppress("MemberVisibilityCanBePrivate")
class FriendMsgCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the sender.
     */
    val sender: SteamID

    /**
     * Gets the chat entry type.
     */
    val entryType: EChatEntryType?

    /**
     * Gets a value indicating whether this message is from a limited account.
     */
    val isFromLimitedAccount: Boolean

    /**
     * Gets the message.
     */
    var message: String? = null
        private set

    init {
        val friendMsg = ClientMsgProtobuf<CMsgClientFriendMsgIncoming.Builder>(
            CMsgClientFriendMsgIncoming::class.java,
            packetMsg
        )
        val msg = friendMsg.body

        sender = SteamID(msg.steamidFrom)
        entryType = EChatEntryType.from(msg.chatEntryType)

        isFromLimitedAccount = msg.fromLimitedAccount

        if (msg.hasMessage()) {
            message = msg.message.toString(StandardCharsets.UTF_8)
            message = message!!.replace("\u0000+$".toRegex(), "") // trim any extra null chars from the end
        }
    }
}
