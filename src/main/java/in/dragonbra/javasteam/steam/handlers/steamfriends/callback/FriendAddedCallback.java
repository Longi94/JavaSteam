package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientAddFriendResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired in response to adding a user to your friends list.
 */
public class FriendAddedCallback extends CallbackMsg {
    private EResult result;

    private SteamID steamID;

    private String personaName;

    public FriendAddedCallback(CMsgClientAddFriendResponse.Builder msg) {
        result = EResult.from(msg.getEresult());

        steamID = new SteamID(msg.getSteamIdAdded());

        personaName = msg.getPersonaNameAdded();
    }

    public EResult getResult() {
        return result;
    }

    public SteamID getSteamID() {
        return steamID;
    }

    public String getPersonaName() {
        return personaName;
    }
}
