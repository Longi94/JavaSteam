package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EAccountFlags
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientAccountInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is received when account information is received from the network.
 * This generally happens after logon.
 */
@Suppress("MemberVisibilityCanBePrivate")
class AccountInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the last recorded persona name used by this account.
     */
    val personaName: String

    /**
     * Gets the country this account is connected from.
     */
    val country: String

    /**
     * Gets the count of SteamGuard authenticated computers.
     */
    val countAuthedComputers: Int

    /**
     * Gets the account flags for this account. See [EAccountFlags].
     */
    val accountFlags: EnumSet<EAccountFlags>

    init {
        val accInfo = ClientMsgProtobuf<CMsgClientAccountInfo.Builder>(CMsgClientAccountInfo::class.java, packetMsg)
        val msg = accInfo.body

        personaName = msg.personaName
        country = msg.ipCountry

        countAuthedComputers = msg.countAuthedComputers

        accountFlags = EAccountFlags.from(msg.accountFlags)
    }
}
