package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received when another client starts or stops playing a game.
 * While blocked, sending ClientGamesPlayed message will log you off with LoggedInElsewhere result.
 */
@Suppress("MemberVisibilityCanBePrivate")
class PlayingSessionStateCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Indicates whether playing is currently blocked by another client.
     */
    val isPlayingBlocked: Boolean

    /**
     * When blocked, gets the appid which is currently being played.
     */
    val playingAppID: Int

    init {
        val playingSessionState = ClientMsgProtobuf<SteammessagesClientserver2.CMsgClientPlayingSessionState.Builder>(
            SteammessagesClientserver2.CMsgClientPlayingSessionState::class.java,
            packetMsg
        )
        val msg = playingSessionState.body

        jobID = playingSessionState.targetJobID

        isPlayingBlocked = msg.playingBlocked
        playingAppID = msg.playingApp
    }
}
