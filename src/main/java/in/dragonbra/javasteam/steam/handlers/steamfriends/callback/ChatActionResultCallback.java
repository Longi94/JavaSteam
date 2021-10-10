package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatAction;
import in.dragonbra.javasteam.enums.EChatActionResult;
import in.dragonbra.javasteam.generated.MsgClientChatActionResult;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when a chat action has completed.
 */
public class ChatActionResultCallback extends CallbackMsg {

    private SteamID chatRoomID;

    private SteamID chatterID;

    private EChatAction action;

    private EChatActionResult result;

    public ChatActionResultCallback(MsgClientChatActionResult result) {
        chatRoomID = result.getSteamIdChat();
        chatterID = result.getSteamIdUserActedOn();

        action = result.getChatAction();
        this.result = result.getActionResult();
    }

    public ChatActionResultCallback(SteamID chatRoomID, SteamID chatterID, EChatAction action, EChatActionResult result) {
        this.chatRoomID = chatRoomID;
        this.chatterID = chatterID;
        this.action = action;
        this.result = result;
    }

    /**
     * @return the SteamID of the chat room the action was performed in.
     */
    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    /**
     * @return the SteamID of the chat member the action was performed on.
     */
    public SteamID getChatterID() {
        return chatterID;
    }

    /**
     * @return the chat action that was performed.
     */
    public EChatAction getAction() {
        return action;
    }

    /**
     * @return the result of the chat action.
     */
    public EChatActionResult getResult() {
        return result;
    }
}
