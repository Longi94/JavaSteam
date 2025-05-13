package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSPrivateBetaResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.stream.MemoryStream

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

        depotSection = KeyValue()

        if (msg.depotSection != null && msg.depotSection.size() > 0) {
            // we don't want to read the trailing null byte
            MemoryStream(msg.depotSection.toByteArray(), 0, msg.depotSection.size() - 1).use { ms ->
                depotSection.readAsText(ms)
            }
        }
    }
}
