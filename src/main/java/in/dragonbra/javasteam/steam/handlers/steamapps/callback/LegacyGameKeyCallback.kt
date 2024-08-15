package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.generated.MsgClientGetLegacyGameKeyResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamApps.getLegacyGameKey].
 */
class LegacyGameKeyCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of requesting this game key.
     */
    val result: EResult

    /**
     * Gets the appid that this game key is for.
     */
    val appID: Int

    /**
     * Gets the game key.
     */
    var key: String? = null

    init {
        val keyResponse = ClientMsg(MsgClientGetLegacyGameKeyResponse::class.java, packetMsg)
        val msg = keyResponse.body

        jobID = keyResponse.targetJobID
        appID = msg.appId
        result = msg.result

        if (msg.length > 0) {
            val length: Int = msg.length - 1
            val payload = keyResponse.payload.toByteArray()
            key = String(payload, 0, length)
        }
    }
}
