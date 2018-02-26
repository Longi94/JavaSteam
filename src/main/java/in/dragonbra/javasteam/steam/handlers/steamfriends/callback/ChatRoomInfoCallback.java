package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatInfoType;
import in.dragonbra.javasteam.generated.MsgClientChatRoomInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired in response to chat room info being recieved.
 */
public class ChatRoomInfoCallback extends CallbackMsg {

    private SteamID chatRoomID;

    private EChatInfoType type;

    public ChatRoomInfoCallback(MsgClientChatRoomInfo msg, byte[] payload) {
        chatRoomID = msg.getSteamIdChat();
        type = msg.getType();
        // todo: handle inner payload based on the type similar to ChatMemberInfoCallback
    }

    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    public EChatInfoType getType() {
        return type;
    }
}
