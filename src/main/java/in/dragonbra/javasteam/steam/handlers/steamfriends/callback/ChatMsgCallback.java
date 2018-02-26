package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.generated.MsgClientChatMsg;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.nio.charset.Charset;

/**
 * This callback is fired when a chat room message arrives.
 */
public class ChatMsgCallback extends CallbackMsg {
    private SteamID chatterID;

    private SteamID chatRoomID;

    private EChatEntryType chatMsgType;

    private String message;

    public ChatMsgCallback(MsgClientChatMsg msg, byte[] payload) {
        chatterID = msg.getSteamIdChatter();
        chatRoomID = msg.getSteamIdChatRoom();
        chatMsgType = msg.getChatMsgType();

        message = new String(payload, Charset.forName("UTF-8")).replaceAll("\0+$", ""); // trim any extra null chars from the end
    }

    public SteamID getChatterID() {
        return chatterID;
    }

    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    public EChatEntryType getChatMsgType() {
        return chatMsgType;
    }

    public String getMessage() {
        return message;
    }
}
