package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSProductInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is fired when the PICS returns the product information requested
 */
@Suppress("MemberVisibilityCanBePrivate")
class PICSProductInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets if this response contains only product metadata.
     */
    val isMetaDataOnly: Boolean

    /**
     * Gets if there are more product information responses pending.
     */
    val isResponsePending: Boolean

    /**
     * Gets a list of unknown package ids.
     */
    val unknownPackages: List<Int>

    /**
     * Gets a list of unknown app ids.
     */
    val unknownApps: List<Int>

    /**
     * Gets a map containing requested app info.
     */
    val apps: Map<Int, PICSProductInfo>

    /**
     * Gets a map containing requested package info.
     */
    val packages: Map<Int, PICSProductInfo>

    init {
        val productResponse = ClientMsgProtobuf<CMsgClientPICSProductInfoResponse.Builder>(
            CMsgClientPICSProductInfoResponse::class.java,
            packetMsg
        )
        val msg = productResponse.body

        jobID = productResponse.targetJobID

        isMetaDataOnly = msg.metaDataOnly
        isResponsePending = msg.responsePending
        unknownPackages = Collections.unmodifiableList(msg.unknownPackageidsList)
        unknownApps = Collections.unmodifiableList(msg.unknownAppidsList)

        apps = msg.appsList.associate { it.appid to PICSProductInfo(msg, it) }
        packages = msg.packagesList.associate { it.packageid to PICSProductInfo(it) }
    }
}
