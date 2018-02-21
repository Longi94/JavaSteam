package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatPermission;
import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientCreateChat implements ISteamSerializableMessage {

    private EChatRoomType chatRoomType = EChatRoomType.from(0);

    private long gameId = 0L;

    private long steamIdClan = 0L;

    private EChatPermission permissionOfficer = EChatPermission.from(0);

    private EChatPermission permissionMember = EChatPermission.from(0);

    private EChatPermission permissionAll = EChatPermission.from(0);

    private long membersMax = 0L;

    private byte chatFlags = (byte) 0;

    private long steamIdFriendChat = 0L;

    private long steamIdInvited = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientCreateChat;
    }

    public EChatRoomType getChatRoomType() {
        return this.chatRoomType;
    }

    public void setChatRoomType(EChatRoomType chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public GameID getGameId() {
        return new GameID(this.gameId);
    }

    public void setGameId(GameID gameId) {
        this.gameId = gameId.convertToUInt64();
    }

    public SteamID getSteamIdClan() {
        return new SteamID(this.steamIdClan);
    }

    public void setSteamIdClan(SteamID steamId) {
        this.steamIdClan = steamId.convertToUInt64();
    }

    public EChatPermission getPermissionOfficer() {
        return this.permissionOfficer;
    }

    public void setPermissionOfficer(EChatPermission permissionOfficer) {
        this.permissionOfficer = permissionOfficer;
    }

    public EChatPermission getPermissionMember() {
        return this.permissionMember;
    }

    public void setPermissionMember(EChatPermission permissionMember) {
        this.permissionMember = permissionMember;
    }

    public EChatPermission getPermissionAll() {
        return this.permissionAll;
    }

    public void setPermissionAll(EChatPermission permissionAll) {
        this.permissionAll = permissionAll;
    }

    public long getMembersMax() {
        return this.membersMax;
    }

    public void setMembersMax(long membersMax) {
        this.membersMax = membersMax;
    }

    public byte getChatFlags() {
        return this.chatFlags;
    }

    public void setChatFlags(byte chatFlags) {
        this.chatFlags = chatFlags;
    }

    public SteamID getSteamIdFriendChat() {
        return new SteamID(this.steamIdFriendChat);
    }

    public void setSteamIdFriendChat(SteamID steamId) {
        this.steamIdFriendChat = steamId.convertToUInt64();
    }

    public SteamID getSteamIdInvited() {
        return new SteamID(this.steamIdInvited);
    }

    public void setSteamIdInvited(SteamID steamId) {
        this.steamIdInvited = steamId.convertToUInt64();
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(chatRoomType.code());
        dos.writeLong(gameId);
        dos.writeLong(steamIdClan);
        dos.writeInt(permissionOfficer.code());
        dos.writeInt(permissionMember.code());
        dos.writeInt(permissionAll.code());
        dos.writeLong(membersMax);
        dos.writeByte(chatFlags);
        dos.writeLong(steamIdFriendChat);
        dos.writeLong(steamIdInvited);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        chatRoomType = EChatRoomType.from(dis.readInt());
        gameId = dis.readLong();
        steamIdClan = dis.readLong();
        permissionOfficer = EChatPermission.from(dis.readInt());
        permissionMember = EChatPermission.from(dis.readInt());
        permissionAll = EChatPermission.from(dis.readInt());
        membersMax = dis.readLong();
        chatFlags = dis.readByte();
        steamIdFriendChat = dis.readLong();
        steamIdInvited = dis.readLong();
    }
}
