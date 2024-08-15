package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientEmailAddrInfo
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received when email information is received from the network.
 */
@Suppress("MemberVisibilityCanBePrivate")
class EmailAddrInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the email address of this account.
     */
    val emailAddress: String

    /**
     * Gets a value indicating validated email or not.
     */
    val isEmailValidated: Boolean

    init {
        val emailAddrInfo = ClientMsgProtobuf<CMsgClientEmailAddrInfo.Builder>(
            CMsgClientEmailAddrInfo::class.java,
            packetMsg
        )
        val msg = emailAddrInfo.body

        emailAddress = msg.emailAddress
        isEmailValidated = msg.emailIsValidated
    }
}
