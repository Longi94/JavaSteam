package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendMsgIncoming;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.nio.charset.Charset;

/**
 * This callback is fired in response to receiving a message from a friend.
 */
public class FriendMsgCallback extends CallbackMsg {
    private SteamID sender;

    private EChatEntryType entryType;

    private boolean fromLimitedAccount;

    private String message;

    public FriendMsgCallback(CMsgClientFriendMsgIncoming.Builder msg) {
        sender = new SteamID(msg.getSteamidFrom());
        entryType = EChatEntryType.from(msg.getChatEntryType());

        fromLimitedAccount = msg.getFromLimitedAccount();

        if (msg.hasMessage()) {
            message = msg.getMessage().toString(Charset.forName("UTF-8"));
            message = message.replaceAll("\0+$", ""); // trim any extra null chars from the end
        }
    }

    public SteamID getSender() {
        return sender;
    }

    public EChatEntryType getEntryType() {
        return entryType;
    }

    public boolean isFromLimitedAccount() {
        return fromLimitedAccount;
    }

    public String getMessage() {
        return message;
    }
}
