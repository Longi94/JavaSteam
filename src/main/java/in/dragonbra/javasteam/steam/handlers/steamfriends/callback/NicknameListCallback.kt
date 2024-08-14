package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPlayerNicknameList;
import in.dragonbra.javasteam.steam.handlers.steamfriends.PlayerNickname;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is fired when the client receives a list of friend nicknames.
 */
public class NicknameListCallback extends CallbackMsg {

    private List<PlayerNickname> nicknames;

    public NicknameListCallback(CMsgClientPlayerNicknameList.Builder msg) {
        nicknames = new ArrayList<>();
        for (CMsgClientPlayerNicknameList.PlayerNickname nickname : msg.getNicknamesList()) {
            nicknames.add(new PlayerNickname(nickname));
        }

        nicknames = Collections.unmodifiableList(nicknames);
    }

    /**
     * @return the list of nicknames
     */
    public List<PlayerNickname> getNicknames() {
        return nicknames;
    }
}
