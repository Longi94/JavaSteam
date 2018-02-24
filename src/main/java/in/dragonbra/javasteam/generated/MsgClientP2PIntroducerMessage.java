package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EIntroducerRouting;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientP2PIntroducerMessage implements ISteamSerializableMessage {

    private long steamID = 0L;

    private EIntroducerRouting routingType = EIntroducerRouting.from(0);

    private byte[] data = new byte[1450];

    private int dataLen = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientP2PIntroducerMessage;
    }

    public SteamID getSteamID() {
        return new SteamID(this.steamID);
    }

    public void setSteamID(SteamID steamId) {
        this.steamID = steamId.convertToUInt64();
    }

    public EIntroducerRouting getRoutingType() {
        return this.routingType;
    }

    public void setRoutingType(EIntroducerRouting routingType) {
        this.routingType = routingType;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getDataLen() {
        return this.dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(steamID);
        bw.writeInt(routingType.code());
        bw.writeInt(data.length);
        bw.write(data);
        bw.writeInt(dataLen);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        steamID = br.readLong();
        routingType = EIntroducerRouting.from(br.readInt());
        data = br.readBytes(br.readInt());
        dataLen = br.readInt();
    }
}
