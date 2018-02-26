package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList;
import in.dragonbra.javasteam.steam.handlers.steamfriends.Friend;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This callback is fired when the client receives a list of friends.
 */
public class FriendsListCallback extends CallbackMsg {
    private boolean incremental;

    private List<Friend> friendList;

    public FriendsListCallback(CMsgClientFriendsList.Builder msg) {
        incremental = msg.getBincremental();

        List<Friend> list = msg.getFriendsList().stream().map(Friend::new).collect(Collectors.toList());

        friendList = Collections.unmodifiableList(list);
    }

    public boolean isIncremental() {
        return incremental;
    }

    public List<Friend> getFriendList() {
        return friendList;
    }
}
