package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EAccountFlags
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.Event
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.util.*

/**
 * This callback is posted when a clan's state has been changed.
 */
class ClanStateCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the [SteamID] of the clan that posted this state update.
     */
    val clanID: SteamID

    /**
     * Gets the account flags.
     */
    val accountFlags: EnumSet<EAccountFlags>

    /**
     * Gets the privacy of the chat room.
     */
    val isChatRoomPrivate: Boolean

    /**
     * Gets the name of the clan.
     */
    var clanName: String? = null
        private set

    /**
     * Gets the SHA-1 avatar hash.
     */
    var avatarHash: ByteArray = byteArrayOf()
        private set

    /**
     * Gets the total number of members in this clan.
     */
    var memberTotalCount: Int = 0
        private set

    /**
     * Gets the number of members in this clan that are currently online.
     */
    var memberOnlineCount: Int = 0
        private set

    /**
     * Gets the number of members in this clan that are currently chatting.
     */
    var memberChattingCount: Int = 0
        private set

    /**
     * Gets the number of members in this clan that are currently in-game.
     */
    var memberInGameCount: Int = 0
        private set

    /**
     * Gets any events associated with this clan state update. See [Event]
     */
    val events: List<Event>

    /**
     * Gets any announcements associated with this clan state update. See [Event]
     */
    val announcements: List<Event>

    init {
        val clanState = ClientMsgProtobuf<CMsgClientClanState.Builder>(CMsgClientClanState::class.java, packetMsg)
        val msg = clanState.body

        clanID = SteamID(msg.steamidClan)

        accountFlags = EAccountFlags.from(msg.clanAccountFlags)
        isChatRoomPrivate = msg.chatRoomPrivate

        if (msg.hasNameInfo()) {
            clanName = msg.nameInfo.clanName
            avatarHash = msg.nameInfo.shaAvatar.toByteArray()
        }

        if (msg.hasUserCounts()) {
            memberTotalCount = msg.userCounts.members
            memberOnlineCount = msg.userCounts.online
            memberChattingCount = msg.userCounts.chatting
            memberInGameCount = msg.userCounts.inGame
        }

        events = msg.eventsList.map { Event(it) }

        announcements = msg.announcementsList.map { Event(it) }
    }
}
