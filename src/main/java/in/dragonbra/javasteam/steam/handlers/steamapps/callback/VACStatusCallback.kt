package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.generated.MsgClientVACBanStatus
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

/**
 * This callback is fired when the client receives its VAC banned status.
 */
class VACStatusCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a list of VAC banned apps the client is banned from.
     */
    val bannedApps: List<Int>

    init {
        val vacStatus = ClientMsg(MsgClientVACBanStatus::class.java, packetMsg)
        val msg = vacStatus.body

        val tempList: MutableList<Int> = ArrayList()

        try {
            BinaryReader(ByteArrayInputStream(vacStatus.payload.toByteArray())).use { br ->
                for (i in 0 until msg.numBans) {
                    tempList.add(br.readInt())
                }
            }
        } catch (e: IOException) {
            throw IllegalArgumentException("failed to read bans", e)
        }

        bannedApps = tempList.toList()
    }
}
