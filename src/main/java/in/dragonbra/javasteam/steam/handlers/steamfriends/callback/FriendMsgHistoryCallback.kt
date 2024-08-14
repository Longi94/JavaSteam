package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.enums.EAccountType
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryResponse
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.FriendMessage
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.util.*

/**
 * This callback is fired in response to receiving historical messages.
 *
 * @see SteamFriends.requestOfflineMessages
 * @see SteamFriends.requestMessageHistory
 */
class FriendMsgHistoryCallback(
    msg: CMsgClientChatGetFriendMessageHistoryResponse.Builder,
    universe: EUniverse?,
) : CallbackMsg() {

    /**
     * Gets the result of the request.
     */
    val result: EResult = EResult.from(msg.success)

    /**
     * Gets the [SteamID] of the user with whom these messages were exchanged.
     */
    val steamID: SteamID = SteamID(msg.steamid)

    /**
     * The messages exchanged with the user.
     * Offline messages are marked by having set [FriendMessage.unread] to **true**
     */
    val messages: List<FriendMessage> = msg.messagesList.map { m ->
        val senderID = SteamID(m.accountid.toLong(), universe, EAccountType.Individual)
        val timestamp = Date(m.timestamp * 1000L)
        FriendMessage(senderID, m.unread, m.message, timestamp)
    }
}
