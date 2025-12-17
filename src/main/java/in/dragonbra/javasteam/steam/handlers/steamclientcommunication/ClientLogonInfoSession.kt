package `in`.dragonbra.javasteam.steam.handlers.steamclientcommunication

import `in`.dragonbra.javasteam.enums.EGamingDeviceType
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.ESteamRealm

/**
 * Describes an active device running desktop Steam client.
 * @param clientInstanceId Instance ID that is used to send remote signals
 * @param protocolVersion Protocol version of the client
 * @param osName Name of installed OS, such as "Linux 6.x"
 * @param machineName  Name of the device, such as "steamdeck"
 * @param osType Type of installed OS, such as [EOSType].k_Linux6x
 * @param deviceType Type of the device, such as [EGamingDeviceType].k_EGamingDeviceType_SteamDeck
 * @param realm Realm of the session from. See [ESteamRealm].
 */
data class ClientLogonInfoSession(
    val clientInstanceId: Long,
    val protocolVersion: Int,
    val osName: String,
    val machineName: String,
    val osType: Int,
    val deviceType: Int,
    val realm: Int,
) {
    override fun toString(): String = """
           ClientLogonInfoSession(
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
