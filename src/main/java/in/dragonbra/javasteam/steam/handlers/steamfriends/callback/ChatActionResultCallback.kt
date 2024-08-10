package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatAction
import `in`.dragonbra.javasteam.enums.EChatActionResult
import `in`.dragonbra.javasteam.generated.MsgClientChatActionResult
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID

/**
 * This callback is fired when a chat action has completed.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class ChatActionResultCallback : CallbackMsg {

    /**
     * Gets the SteamID of the chat room the action was performed in.
     */
    val chatRoomID: SteamID

    /**
     * Gets the SteamID of the chat member the action was performed on.
     */
    val chatterID: SteamID

    /**
     * Gets the chat action that was performed.
     */
    val action: EChatAction

    /**
     * Gets the result of the chat action.
     */
    val result: EChatActionResult

    constructor(packetMsg: IPacketMsg) {
        val actionResult = ClientMsg(MsgClientChatActionResult::class.java, packetMsg)
        val result = actionResult.body

        this.chatRoomID = result.steamIdChat
        this.chatterID = result.steamIdUserActedOn
        this.action = result.chatAction
        this.result = result.actionResult
    }

    constructor(chatRoomID: SteamID, chatterID: SteamID, action: EChatAction, result: EChatActionResult) {
        this.chatRoomID = chatRoomID
        this.chatterID = chatterID
        this.action = action
        this.result = result
    }
}
