package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgChannelEncryptResponse implements ISteamSerializableMessage {

    private long protocolVersion = MsgChannelEncryptRequest.PROTOCOL_VERSION;

    private long keySize = 128L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ChannelEncryptResponse;
    }

    public long getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(long protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public long getKeySize() {
        return this.keySize;
    }

    public void setKeySize(long keySize) {
        this.keySize = keySize;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(protocolVersion);
        dos.writeLong(keySize);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        protocolVersion = br.readLong();
        keySize = br.readLong();
    }
}
