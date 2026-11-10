package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSPrivateBetaResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSProductInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.log.LogManager

/**
 * This callback is received when a private beta request has been completed
 */
class PrivateBetaCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Result of the operation
     */
    val result: EResult

    /**
     * Gets the keyvalue info to be merged into main appinfo
     */
    val depotSection: KeyValue

    init {
        val response = ClientMsgProtobuf<CMsgClientPICSPrivateBetaResponse.Builder>(
            CMsgClientPICSPrivateBetaResponse::class.java,
            packetMsg
        )
        val msg = response.body

        jobID = response.targetJobID

        result = EResult.from(msg.eresult)

        depotSection = if (msg.depotSection.isEmpty) {
            KeyValue()
        } else {
            PICSProductInfo.readTextBuffer(msg.depotSection.toByteArray()) { error ->
                logger.error("Failed to load text buffer for depot section", error)
            }
        }
    }

    companion object {
        private val logger = LogManager.getLogger<PrivateBetaCallback>()
    }
}
