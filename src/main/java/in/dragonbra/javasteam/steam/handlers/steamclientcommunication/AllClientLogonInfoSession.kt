package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.enums.EGamingDeviceType
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.ESteamRealm
import `in`.dragonbra.javasteam.util.JavaSteamAddition

/**
 * Describes an active device running a Steam client.
 * @param clientInstanceId Instance ID used to send remote signals to this client.
 * @param protocolVersion Protocol version of the client.
 * @param osName Name of the installed OS, such as "Linux 6.x".
 * @param machineName Name of the device, such as "steamdeck".
 * @param osType Type of installed OS, such as [EOSType].Linux6x.
 * @param deviceType Type of the device, such as [EGamingDeviceType].StandardPC.
 * @param realm Realm of the session. See [ESteamRealm].
 */
@JavaSteamAddition
data class AllClientLogonInfoSession(
    val clientInstanceId: Long,
    val protocolVersion: Int,
    val osName: String,
    val machineName: String,
    val osType: EOSType,
    val deviceType: EGamingDeviceType,
    val realm: ESteamRealm,
) {
    override fun toString(): String = """
           AllClientLogonInfoSession(
                clientInstanceId=$clientInstanceId,
                protocolVersion=$protocolVersion,
                osName='$osName',
                machineName='$machineName',
                osType=$osType,
                deviceType=$deviceType,
                realm=$realm
           )
    """.trimIndent()
}
