package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRedeemGuestPassResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to activating a guest pass or a gift.
 */
@Suppress("MemberVisibilityCanBePrivate")
class RedeemGuestPassResponseCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets Result of the operation
     */
    val result: EResult

    /**
     * Gets Result of the operation
     */
    val packageID: Int

    /**
     * Gets App ID which must be owned to activate this guest pass.
     */
    val mustOwnAppID: Int

    init {
        val redeemedGuestPass = ClientMsgProtobuf<CMsgClientRedeemGuestPassResponse.Builder>(
            CMsgClientRedeemGuestPassResponse::class.java,
            packetMsg
        )
        val msg = redeemedGuestPass.body

        jobID = redeemedGuestPass.targetJobID
        result = EResult.from(msg.eresult)
        packageID = msg.packageId
        mustOwnAppID = msg.mustOwnAppid
    }
}
