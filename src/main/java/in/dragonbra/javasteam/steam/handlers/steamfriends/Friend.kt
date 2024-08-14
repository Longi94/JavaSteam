package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList
import `in`.dragonbra.javasteam.types.SteamID

/**
 * Represents a single friend entry in a client's friendlist.
 */
class Friend(friend: CMsgClientFriendsList.Friend) {

    /**
     * Gets the [SteamID] of the friend.
     */
    val steamID: SteamID = SteamID(friend.ulfriendid)

    /**
     * Gets the relationship to this friend.
     */
    val relationship: EFriendRelationship = EFriendRelationship.from(friend.efriendrelationship)
}
