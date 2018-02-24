package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgChannelEncryptResponse implements ISteamSerializableMessage {

    private int protocolVersion = MsgChannelEncryptRequest.PROTOCOL_VERSION;

    private int keySize = 128;

    @Override
    public EMsg getEMsg() {
        return EMsg.ChannelEncryptResponse;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getKeySize() {
        return this.keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(protocolVersion);
        bw.writeInt(keySize);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        protocolVersion = br.readInt();
        keySize = br.readInt();
    }
}
