package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSChangeData
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired when the PICS return the changes since the last change number
 */
class PICSChangesCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the supplied change number for the request.
     */
    val lastChangeNumber: Int

    /**
     * Gets the current change number.
     */
    val currentChangeNumber: Int

    /**
     * Gets if this update requires a full update of the information.
     */
    val isRequiresFullUpdate: Boolean

    /**
     * Gets if this update requires a full update of the package information.
     */
    val isRequiresFullAppUpdate: Boolean

    /**
     * Gets if this update requires a full update of the app information.
     */
    val isRequiresFullPackageUpdate: Boolean

    /**
     * Gets a map containing requested package tokens.
     */
    val packageChanges: Map<Int, PICSChangeData>

    /**
     * Gets a map containing requested package tokens.
     */
    val appChanges: Map<Int, PICSChangeData>

    init {
        val changesResponse = ClientMsgProtobuf<CMsgClientPICSChangesSinceResponse.Builder>(
            CMsgClientPICSChangesSinceResponse::class.java,
            packetMsg
        )
        val msg = changesResponse.body

        jobID = changesResponse.targetJobID

        lastChangeNumber = msg.sinceChangeNumber
        currentChangeNumber = msg.currentChangeNumber
        isRequiresFullAppUpdate = msg.forceFullAppUpdate
        isRequiresFullPackageUpdate = msg.forceFullPackageUpdate
        isRequiresFullUpdate = msg.forceFullUpdate
        packageChanges = msg.packageChangesList.associate { it.packageid to PICSChangeData(it) }
        appChanges = msg.appChangesList.associate { it.appid to PICSChangeData(it) }
    }
}
