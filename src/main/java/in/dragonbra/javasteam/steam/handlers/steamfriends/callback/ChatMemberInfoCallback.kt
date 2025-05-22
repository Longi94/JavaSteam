package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatInfoType
import `in`.dragonbra.javasteam.enums.EChatMemberStateChange
import `in`.dragonbra.javasteam.generated.MsgClientChatMemberInfo
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.ChatMemberInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException
import java.util.*

/**
 * This callback is fired in response to chat member info being recieved.
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChatMemberInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    companion object {
        private val logger = LogManager.getLogger(ChatMemberInfoCallback::class.java)
    }

    /**
     * Gets SteamId of the chat room.
     */
    val chatRoomID: SteamID

    /**
     * Gets the info type.
     */
    val type: EChatInfoType

    /**
     * Gets the state change info for [EChatInfoType.StateChange] member info updates.
     */
    var stateChangeInfo: StateChangeDetails? = null

    init {
        val membInfo = ClientMsg(MsgClientChatMemberInfo::class.java, packetMsg)
        val msg = membInfo.body

        chatRoomID = msg.steamIdChat
        type = msg.type

        when (type) {
            EChatInfoType.StateChange -> stateChangeInfo = StateChangeDetails(membInfo.payload)
            // todo: (SK) handle more types
            // based off disassembly
            //   - for InfoUpdate, a ChatMemberInfo object is present
            //   - for MemberLimitChange, looks like an ignored uint64 (probably steamid) followed
            //     by an int which likely represents the member limit
            else -> Unit
        }
    }

    /**
     * Represents state change information.
     */
    class StateChangeDetails(ms: MemoryStream) {
        /**
         * Gets the [SteamID] of the chatter that was acted on.
         */
        var chatterActedOn: SteamID? = null

        /**
         * Gets the state change for the acted on SteamID.
         */
        var stateChange: EnumSet<EChatMemberStateChange>? = null

        /**
         * Gets the [SteamID] of the chatter that acted on [StateChangeDetails.chatterActedOn].
         */
        var chatterActedBy: SteamID? = null

        /**
         * This field is only populated when [StateChangeDetails.stateChange] is [EChatMemberStateChange.Entered].
         * Gets the member information for a user that has joined the chat room.
         */
        var memberInfo: ChatMemberInfo? = null

        init {
            try {
                BinaryReader(ms).use { br ->
                    chatterActedOn = SteamID(br.readLong())
                    stateChange = EChatMemberStateChange.from(br.readInt())
                    chatterActedBy = SteamID(br.readLong())
                    if (stateChange!!.contains(EChatMemberStateChange.Entered)) {
                        memberInfo = ChatMemberInfo()
                        memberInfo!!.readFromStream(br)
                    }
                }
            } catch (e: IOException) {
                logger.error("Failed to read chat member info", e)
            }
        }
    }
}
