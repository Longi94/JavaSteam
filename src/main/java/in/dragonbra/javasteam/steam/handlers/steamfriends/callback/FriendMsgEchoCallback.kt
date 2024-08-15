package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendMsgIncoming
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.nio.charset.StandardCharsets

/**
 * This callback is fired in response to receiving an echo message from another instance.
 */
class FriendMsgEchoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets or sets the recipient
     */
    val recipient: SteamID

    /**
     * Gets the chat entry type.
     */
    val entryType: EChatEntryType

    /**
     * Gets a value indicating whether this message is from a limited account.
     */
    val fromLimitedAccount: Boolean

    /**
     * Gets the message.
     */
    var message: String? = null
        private set

    /**
     * @return The timestamp from the server.
     */
    val rTime32ServerTimestamp: Int

    init {
        val friendEchoMsg = ClientMsgProtobuf<CMsgClientFriendMsgIncoming.Builder>(
            CMsgClientFriendMsgIncoming::class.java,
            packetMsg
        )
        val msg = friendEchoMsg.body

        recipient = SteamID(msg.steamidFrom)
        entryType = EChatEntryType.from(msg.chatEntryType)

        fromLimitedAccount = msg.fromLimitedAccount

        if (msg.hasMessage()) {
            message = msg.message.toString(StandardCharsets.UTF_8)
            message = message!!.replace("\u0000+$".toRegex(), "") // trim any extra null chars from the end
        }

        rTime32ServerTimestamp = msg.rtime32ServerTimestamp
    }
}
