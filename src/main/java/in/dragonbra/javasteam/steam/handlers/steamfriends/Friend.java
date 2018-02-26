package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList;
import in.dragonbra.javasteam.types.SteamID;

/**
 * Represents a single friend entry in a client's friendlist.
 */
public class Friend {
    private SteamID steamID;

    private EFriendRelationship relationship;

    public Friend(CMsgClientFriendsList.Friend friend) {
        steamID = new SteamID(friend.getUlfriendid());
        relationship = EFriendRelationship.from(friend.getEfriendrelationship());
    }

    public SteamID getSteamID() {
        return steamID;
    }

    public EFriendRelationship getRelationship() {
        return relationship;
    }
}
