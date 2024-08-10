package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientLicenseList
import `in`.dragonbra.javasteam.steam.handlers.steamapps.License
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is fired during logon, informing the client of it's available licenses.
 */
class LicenseListCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the message.
     */
    val result: EResult

    /**
     * Gets the license list.
     */
    val licenseList: List<License>

    init {
        val licenseListResp = ClientMsgProtobuf<CMsgClientLicenseList.Builder>(
            CMsgClientLicenseList::class.java,
            packetMsg
        )
        val msg = licenseListResp.body

        result = EResult.from(msg.eresult)

        licenseList = msg.licensesList.map { License(it) }
    }
}
