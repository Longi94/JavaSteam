package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientChatInvite;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when a chat invite is recieved.
 */
public class ChatInviteCallback extends CallbackMsg {

    private SteamID invitedID;

    private SteamID chatRoomID;

    private SteamID patronID;

    private EChatRoomType chatRoomType;

    private SteamID friendChatID;

    private String chatRoomName;

    private GameID gameID;

    public ChatInviteCallback(CMsgClientChatInvite.Builder invite) {
        invitedID = new SteamID(invite.getSteamIdInvited());
        chatRoomID = new SteamID(invite.getSteamIdChat());

        patronID = new SteamID(invite.getSteamIdPatron());

        chatRoomType = EChatRoomType.from(invite.getChatroomType());

        friendChatID = new SteamID(invite.getSteamIdFriendChat());

        chatRoomName = invite.getChatName();
        gameID = new GameID(invite.getGameId());
    }

    public SteamID getInvitedID() {
        return invitedID;
    }

    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    public SteamID getPatronID() {
        return patronID;
    }

    public EChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    public SteamID getFriendChatID() {
        return friendChatID;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public GameID getGameID() {
        return gameID;
    }
}
