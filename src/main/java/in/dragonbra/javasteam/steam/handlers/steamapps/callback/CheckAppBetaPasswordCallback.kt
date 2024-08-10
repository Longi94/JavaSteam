package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCheckAppBetaPasswordResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.util.Strings

/**
 * This callback is received when a beta password check has been completed
 */
class CheckAppBetaPasswordCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the operation.
     */
    val result: EResult

    /**
     * Gets a map of beta names to their encryption keys.
     */
    val betaPasswords: Map<String, ByteArray>

    init {
        val response = ClientMsgProtobuf<CMsgClientCheckAppBetaPasswordResponse.Builder>(
            CMsgClientCheckAppBetaPasswordResponse::class.java,
            packetMsg
        )
        val msg = response.body

        jobID = response.targetJobID

        result = EResult.from(msg.eresult)
        betaPasswords = msg.betapasswordsList.associate { it.betaname to Strings.decodeHex(it.betapassword) }
    }
}
