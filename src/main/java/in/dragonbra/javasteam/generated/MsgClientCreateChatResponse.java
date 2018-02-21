package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientCreateChatResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private long steamIdChat = 0L;

    private EChatRoomType chatRoomType = EChatRoomType.from(0);

    private long steamIdFriendChat = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientCreateChatResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public EChatRoomType getChatRoomType() {
        return this.chatRoomType;
    }

    public void setChatRoomType(EChatRoomType chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public SteamID getSteamIdFriendChat() {
        return new SteamID(this.steamIdFriendChat);
    }

    public void setSteamIdFriendChat(SteamID steamId) {
        this.steamIdFriendChat = steamId.convertToUInt64();
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeLong(steamIdChat);
        dos.writeInt(chatRoomType.code());
        dos.writeLong(steamIdFriendChat);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        result = EResult.from(dis.readInt());
        steamIdChat = dis.readLong();
        chatRoomType = EChatRoomType.from(dis.readInt());
        steamIdFriendChat = dis.readLong();
    }
}
