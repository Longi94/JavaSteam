package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatRoomEnterResponse;
import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientChatEnter implements ISteamSerializableMessage {

    private long steamIdChat = 0L;

    private long steamIdFriend = 0L;

    private EChatRoomType chatRoomType = EChatRoomType.from(0);

    private long steamIdOwner = 0L;

    private long steamIdClan = 0L;

    private byte chatFlags = (byte) 0;

    private EChatRoomEnterResponse enterResponse = EChatRoomEnterResponse.from(0);

    private int numMembers = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientChatEnter;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public SteamID getSteamIdFriend() {
        return new SteamID(this.steamIdFriend);
    }

    public void setSteamIdFriend(SteamID steamId) {
        this.steamIdFriend = steamId.convertToUInt64();
    }

    public EChatRoomType getChatRoomType() {
        return this.chatRoomType;
    }

    public void setChatRoomType(EChatRoomType chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public SteamID getSteamIdOwner() {
        return new SteamID(this.steamIdOwner);
    }

    public void setSteamIdOwner(SteamID steamId) {
        this.steamIdOwner = steamId.convertToUInt64();
    }

    public SteamID getSteamIdClan() {
        return new SteamID(this.steamIdClan);
    }

    public void setSteamIdClan(SteamID steamId) {
        this.steamIdClan = steamId.convertToUInt64();
    }

    public byte getChatFlags() {
        return this.chatFlags;
    }

    public void setChatFlags(byte chatFlags) {
        this.chatFlags = chatFlags;
    }

    public EChatRoomEnterResponse getEnterResponse() {
        return this.enterResponse;
    }

    public void setEnterResponse(EChatRoomEnterResponse enterResponse) {
        this.enterResponse = enterResponse;
    }

    public int getNumMembers() {
        return this.numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(steamIdChat);
        dos.writeLong(steamIdFriend);
        dos.writeInt(chatRoomType.code());
        dos.writeLong(steamIdOwner);
        dos.writeLong(steamIdClan);
        dos.writeByte(chatFlags);
        dos.writeInt(enterResponse.code());
        dos.writeInt(numMembers);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        steamIdChat = dis.readLong();
        steamIdFriend = dis.readLong();
        chatRoomType = EChatRoomType.from(dis.readInt());
        steamIdOwner = dis.readLong();
        steamIdClan = dis.readLong();
        chatFlags = dis.readByte();
        enterResponse = EChatRoomEnterResponse.from(dis.readInt());
        numMembers = dis.readInt();
    }
}
