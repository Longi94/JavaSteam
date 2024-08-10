package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.Friend
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import java.util.*

/**
 * This callback is fired when the client receives a list of friends.
 */
class FriendsListCallback(msg: CMsgClientFriendsList.Builder) : CallbackMsg() {

    /**
     * Gets a value indicating whether this [FriendsListCallback] is an incremental update.
     * @return **true** if incremental; otherwise, **false**.
     */
    val isIncremental: Boolean = msg.bincremental

    /**
     * Gets the friend list.
     */
    val friendList: List<Friend> = msg.friendsList.map { Friend(it) }
}
