package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList;
import in.dragonbra.javasteam.types.SteamID;

/**
 * Represents a single friend entry in a client's friendlist.
 */
public class Friend {

    private final SteamID steamID;

    private final EFriendRelationship relationship;

    public Friend(CMsgClientFriendsList.Friend friend) {
        steamID = new SteamID(friend.getUlfriendid());
        relationship = EFriendRelationship.from(friend.getEfriendrelationship());
    }

    /**
     * @return the {@link SteamID} of the friend.
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return the relationship to this friend.
     */
    public EFriendRelationship getRelationship() {
        return relationship;
    }
}
