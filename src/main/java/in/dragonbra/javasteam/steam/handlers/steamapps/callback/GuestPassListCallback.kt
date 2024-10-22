package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.generated.MsgClientUpdateGuestPassesList
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.log.LogManager
import java.io.IOException

/**
 * This callback is received when the list of guest passes is updated.
 */
class GuestPassListCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    companion object {
        private val logger = LogManager.getLogger(GuestPassListCallback::class.java)
    }

    /**
     * Gets the result of the operation.
     */
    val result: EResult

    /**
     * Gets the number of guest passes to be given out.
     */
    val countGuestPassesToGive: Int

    /**
     * Gets the number of guest passes to be redeemed.
     */
    val countGuestPassesToRedeem: Int

    /**
     * Gets the guest pass list.
     */
    val guestPasses: List<KeyValue>

    init {
        val guestPassesResp = ClientMsg(MsgClientUpdateGuestPassesList::class.java, packetMsg)
        val msg = guestPassesResp.body

        result = msg.result
        countGuestPassesToGive = msg.countGuestPassesToGive
        countGuestPassesToRedeem = msg.countGuestPassesToRedeem

        val tempList = mutableListOf<KeyValue>()
        try {
            for (i in 0 until countGuestPassesToGive + countGuestPassesToRedeem) {
                val kv = KeyValue()
                kv.tryReadAsBinary(guestPassesResp.payload)
                tempList.add(kv)
            }
        } catch (e: IOException) {
            logger.error("failed to read guest passes", e)
        }

        guestPasses = tempList.toList()
    }
}
