package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientVanityURLChangedNotification
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received when users' vanity url changes.
 */
class VanityURLChangedCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the new vanity url.
     */
    val vanityUrl: String

    init {
        val vanityUrl = ClientMsgProtobuf<CMsgClientVanityURLChangedNotification.Builder>(
            CMsgClientVanityURLChangedNotification::class.java,
            packetMsg
        )

        jobID = vanityUrl.targetJobID

        this.vanityUrl = vanityUrl.body.vanityUrl
    }
}
