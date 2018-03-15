package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientSetPlayerNicknameResponse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired in response to setting a nickname of a player by calling {@link SteamFriends#setFriendNickname(SteamID, String)}.
 */
public class NicknameCallback extends CallbackMsg {

    private EResult result;

    public NicknameCallback(CMsgClientSetPlayerNicknameResponse.Builder body) {
        result = EResult.from(body.getEresult());
    }

    /**
     * @return the result of setting a nickname
     */
    public EResult getResult() {
        return result;
    }
}
