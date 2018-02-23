package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientChatMsg implements ISteamSerializableMessage {

    private long steamIdChatter = 0L;

    private long steamIdChatRoom = 0L;

    private EChatEntryType chatMsgType = EChatEntryType.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientChatMsg;
    }

    public SteamID getSteamIdChatter() {
        return new SteamID(this.steamIdChatter);
    }

    public void setSteamIdChatter(SteamID steamId) {
        this.steamIdChatter = steamId.convertToUInt64();
    }

    public SteamID getSteamIdChatRoom() {
        return new SteamID(this.steamIdChatRoom);
    }

    public void setSteamIdChatRoom(SteamID steamId) {
        this.steamIdChatRoom = steamId.convertToUInt64();
    }

    public EChatEntryType getChatMsgType() {
        return this.chatMsgType;
    }

    public void setChatMsgType(EChatEntryType chatMsgType) {
        this.chatMsgType = chatMsgType;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(steamIdChatter);
        bw.writeLong(steamIdChatRoom);
        bw.writeInt(chatMsgType.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        steamIdChatter = br.readLong();
        steamIdChatRoom = br.readLong();
        chatMsgType = EChatEntryType.from(br.readInt());
    }
}
