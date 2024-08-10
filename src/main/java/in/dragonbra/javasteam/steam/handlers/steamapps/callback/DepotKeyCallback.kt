package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetDepotDecryptionKeyResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received in response to calling [SteamApps.getDepotDecryptionKey]
 */
class DepotKeyCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of requesting this encryption key.
     */
    val result: EResult

    /**
     * Gets the DepotID this encryption key is for.
     */
    val depotID: Int

    /**
     * Gets the encryption key for this depot.
     */
    val depotKey: ByteArray

    init {
        val keyResponse = ClientMsgProtobuf<CMsgClientGetDepotDecryptionKeyResponse.Builder>(
            CMsgClientGetDepotDecryptionKeyResponse::class.java,
            packetMsg
        )
        val msg = keyResponse.body

        jobID = keyResponse.targetJobID

        result = EResult.from(msg.eresult)
        depotID = msg.depotId
        depotKey = msg.depotEncryptionKey.toByteArray()
    }
}
