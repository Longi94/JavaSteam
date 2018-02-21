package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatInfoType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientChatRoomInfo implements ISteamSerializableMessage {

    private long steamIdChat = 0L;

    private EChatInfoType type = EChatInfoType.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientChatRoomInfo;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public EChatInfoType getType() {
        return this.type;
    }

    public void setType(EChatInfoType type) {
        this.type = type;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(steamIdChat);
        dos.writeInt(type.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        steamIdChat = dis.readLong();
        type = EChatInfoType.from(dis.readInt());
    }
}
