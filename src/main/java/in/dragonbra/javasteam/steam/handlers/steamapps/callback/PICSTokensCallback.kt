package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSAccessTokenResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the PICS returns access tokens for a list of appids and packageids
 */
class PICSTokensCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a list of denied package tokens.
     */
    val packageTokensDenied: List<Int>

    /**
     * Gets a list of denied app tokens.
     */
    val appTokensDenied: List<Int>

    /**
     * Gets a map containing requested package tokens.
     */
    val packageTokens: Map<Int, Long>

    /**
     * Gets a map containing requested package tokens.
     */
    val appTokens: Map<Int, Long>

    init {
        val tokensResponse = ClientMsgProtobuf<CMsgClientPICSAccessTokenResponse.Builder>(
            CMsgClientPICSAccessTokenResponse::class.java,
            packetMsg
        )
        val msg = tokensResponse.body

        jobID = tokensResponse.targetJobID

        packageTokensDenied = msg.packageDeniedTokensList
        appTokensDenied = msg.appDeniedTokensList

        packageTokens = msg.packageAccessTokensList.associate { it.packageid to it.accessToken }
        appTokens = msg.appAccessTokensList.associate { it.appid to it.accessToken }
    }
}
