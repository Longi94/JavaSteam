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

    private final SteamID invitedID;

    private final SteamID chatRoomID;

    private final SteamID patronID;

    private final EChatRoomType chatRoomType;

    private final SteamID friendChatID;

    private final String chatRoomName;

    private final GameID gameID;

    public ChatInviteCallback(CMsgClientChatInvite.Builder invite) {
        invitedID = new SteamID(invite.getSteamIdInvited());
        chatRoomID = new SteamID(invite.getSteamIdChat());

        patronID = new SteamID(invite.getSteamIdPatron());

        chatRoomType = EChatRoomType.from(invite.getChatroomType());

        friendChatID = new SteamID(invite.getSteamIdFriendChat());

        chatRoomName = invite.getChatName();
        gameID = new GameID(invite.getGameId());
    }

    /**
     * @return the {@link SteamID} of the user who was invited to the chat.
     */
    public SteamID getInvitedID() {
        return invitedID;
    }

    /**
     * @return the chat room {@link SteamID}.
     */
    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    /**
     * @return the {@link SteamID} of the user who performed the invitation.
     */
    public SteamID getPatronID() {
        return patronID;
    }

    /**
     * @return the chat room type.
     */
    public EChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    /**
     * @return the {@link SteamID} of the chat friend.
     */
    public SteamID getFriendChatID() {
        return friendChatID;
    }

    /**
     * @return the name of the chat room.
     */
    public String getChatRoomName() {
        return chatRoomName;
    }

    /**
     * @return the {@link GameID} associated with this chat room, if it's a game lobby.
     */
    public GameID getGameID() {
        return gameID;
    }
}
