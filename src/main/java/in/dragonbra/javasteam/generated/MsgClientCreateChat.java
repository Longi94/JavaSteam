package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatPermission;
import in.dragonbra.javasteam.enums.EChatRoomType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;

public class MsgClientCreateChat implements ISteamSerializableMessage {

    private EChatRoomType chatRoomType = EChatRoomType.from(0);

    private long gameId = 0L;

    private long steamIdClan = 0L;

    private EnumSet<EChatPermission> permissionOfficer = EChatPermission.from(0);

    private EnumSet<EChatPermission> permissionMember = EChatPermission.from(0);

    private EnumSet<EChatPermission> permissionAll = EChatPermission.from(0);

    private int membersMax = 0;

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

    public EnumSet<EChatPermission> getPermissionOfficer() {
        return this.permissionOfficer;
    }

    public void setPermissionOfficer(EnumSet<EChatPermission> permissionOfficer) {
        this.permissionOfficer = permissionOfficer;
    }

    public EnumSet<EChatPermission> getPermissionMember() {
        return this.permissionMember;
    }

    public void setPermissionMember(EnumSet<EChatPermission> permissionMember) {
        this.permissionMember = permissionMember;
    }

    public EnumSet<EChatPermission> getPermissionAll() {
        return this.permissionAll;
    }

    public void setPermissionAll(EnumSet<EChatPermission> permissionAll) {
        this.permissionAll = permissionAll;
    }

    public int getMembersMax() {
        return this.membersMax;
    }

    public void setMembersMax(int membersMax) {
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
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(chatRoomType.code());
        bw.writeLong(gameId);
        bw.writeLong(steamIdClan);
        bw.writeInt(EChatPermission.code(permissionOfficer));
        bw.writeInt(EChatPermission.code(permissionMember));
        bw.writeInt(EChatPermission.code(permissionAll));
        bw.writeInt(membersMax);
        bw.writeByte(chatFlags);
        bw.writeLong(steamIdFriendChat);
        bw.writeLong(steamIdInvited);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        chatRoomType = EChatRoomType.from(br.readInt());
        gameId = br.readLong();
        steamIdClan = br.readLong();
        permissionOfficer = EChatPermission.from(br.readInt());
        permissionMember = EChatPermission.from(br.readInt());
        permissionAll = EChatPermission.from(br.readInt());
        membersMax = br.readInt();
        chatFlags = br.readByte();
        steamIdFriendChat = br.readLong();
        steamIdInvited = br.readLong();
    }
}
