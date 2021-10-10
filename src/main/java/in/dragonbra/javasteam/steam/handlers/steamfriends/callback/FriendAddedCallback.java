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

    /**
     * @return the result of the request.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the {@link SteamID} of the friend that was added.
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return the persona name of the friend.
     */
    public String getPersonaName() {
        return personaName;
    }
}
