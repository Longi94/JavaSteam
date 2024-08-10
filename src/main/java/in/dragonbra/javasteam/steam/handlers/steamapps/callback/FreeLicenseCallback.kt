package `in`.dragonbra.javasteam.steam.handlers.steamapps.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestFreeLicenseResponse
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is received in response to calling [SteamApps.requestFreeLicense], informing the client of newly granted packages, if any.
 */
@Suppress("MemberVisibilityCanBePrivate")
class FreeLicenseCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the result of the message.
     */
    val result: EResult

    /**
     * Gets the list of granted apps.
     */
    val grantedApps: List<Int>

    /**
     * Gets the list of granted packages.
     */
    val grantedPackages: List<Int>

    init {
        val grantedLicenses = ClientMsgProtobuf<CMsgClientRequestFreeLicenseResponse.Builder>(
            CMsgClientRequestFreeLicenseResponse::class.java,
            packetMsg
        )
        val msg = grantedLicenses.body

        jobID = grantedLicenses.targetJobID

        result = EResult.from(msg.eresult)

        grantedApps = Collections.unmodifiableList(msg.grantedAppidsList)
        grantedPackages = Collections.unmodifiableList(msg.grantedPackageidsList)
    }
}
