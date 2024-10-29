package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EPurchaseResultDetail
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPurchaseResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.stream.MemoryStream

/**
 * This callback is received in a response to activating a Steam key.
 */
@Suppress("MemberVisibilityCanBePrivate")
class PurchaseResponseCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    companion object {
        private val logger = LogManager.getLogger(PurchaseResponseCallback::class.java)
    }

    /**
     * Gets Result of the operation
     */
    val result: EResult

    /**
     * Gets Purchase result of the operation
     */
    val purchaseResultDetail: EPurchaseResultDetail

    /**
     * Gets Purchase receipt of the operation
     */
    val purchaseReceiptInfo: KeyValue

    init {
        val purchaseResponse = ClientMsgProtobuf<CMsgClientPurchaseResponse.Builder>(
            CMsgClientPurchaseResponse::class.java,
            packetMsg
        )
        val msg = purchaseResponse.body

        jobID = purchaseResponse.targetJobID
        result = EResult.from(msg.eresult)
        purchaseResultDetail = EPurchaseResultDetail.from(msg.purchaseResultDetails)
        purchaseReceiptInfo = KeyValue()

        if (msg.purchaseReceiptInfo != null) {
            try {
                val ms = MemoryStream(msg.purchaseReceiptInfo.toByteArray())
                purchaseReceiptInfo.tryReadAsBinary(ms)
            } catch (e: Exception) {
                logger.error("Failed to read purchase receipt info", e)
            }
        }
    }
}
