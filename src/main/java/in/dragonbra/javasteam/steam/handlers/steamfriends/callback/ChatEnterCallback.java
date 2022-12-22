package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatRoomEnterResponse;
import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.generated.MsgClientChatEnter;
import in.dragonbra.javasteam.steam.handlers.steamfriends.ChatMemberInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is fired in response to attempting to join a chat.
 */
public class ChatEnterCallback extends CallbackMsg {

    private SteamID chatID;

    private SteamID friendID;

    private EChatRoomType chatRoomType;

    private SteamID ownerID;

    private SteamID clanID;

    private byte chatFlags;

    private EChatRoomEnterResponse enterResponse;

    private int numChatMembers;

    private String chatRoomName;

    private List<ChatMemberInfo> chatMembers;

    public ChatEnterCallback(MsgClientChatEnter msg, byte[] payload) {
        chatID = msg.getSteamIdChat();
        friendID = msg.getSteamIdFriend();

        chatRoomType = msg.getChatRoomType();

        ownerID = msg.getSteamIdOwner();
        clanID = msg.getSteamIdClan();

        chatFlags = msg.getChatFlags();

        enterResponse = msg.getEnterResponse();

        numChatMembers = msg.getNumMembers();

        ByteArrayInputStream bais = new ByteArrayInputStream(payload);

        try (BinaryReader br = new BinaryReader(bais)) {
            // steamclient always attempts to read the chat room name, regardless of the enter response
            chatRoomName = br.readNullTermString(StandardCharsets.UTF_8);

            if (enterResponse != EChatRoomEnterResponse.Success) {
                // the rest of the payload depends on a successful chat enter
                return;
            }

            List<ChatMemberInfo> memberList = new ArrayList<>();

            for (int i = 0; i < numChatMembers; i++) {
                ChatMemberInfo memberInfo = new ChatMemberInfo();
                memberInfo.readFromStream(br);

                memberList.add(memberInfo);
            }

            chatMembers = Collections.unmodifiableList(memberList);
        } catch (IOException ignored) {
        }
    }

    /**
     * @return the {@link SteamID} of the chat room.
     */
    public SteamID getChatID() {
        return chatID;
    }

    /**
     * @return the friend ID.
     */
    public SteamID getFriendID() {
        return friendID;
    }

    /**
     * @return the type of the chat room.
     */
    public EChatRoomType getChatRoomType() {
        return chatRoomType;
    }

    /**
     * @return the {@link SteamID} of the chat room owner.
     */
    public SteamID getOwnerID() {
        return ownerID;
    }

    /**
     * @return the clan {@link SteamID} that owns this chat room.
     */
    public SteamID getClanID() {
        return clanID;
    }

    /**
     * @return the chat flags.
     */
    public byte getChatFlags() {
        return chatFlags;
    }

    /**
     * @return the chat enter response.
     */
    public EChatRoomEnterResponse getEnterResponse() {
        return enterResponse;
    }

    /**
     * @return the number of users currently in this chat room.
     */
    public int getNumChatMembers() {
        return numChatMembers;
    }

    /**
     * @return the name of the chat room.
     */
    public String getChatRoomName() {
        return chatRoomName;
    }

    /**
     * @return a list of {@link ChatMemberInfo} instances for each of the members of this chat room.
     */
    public List<ChatMemberInfo> getChatMembers() {
        return chatMembers;
    }
}
