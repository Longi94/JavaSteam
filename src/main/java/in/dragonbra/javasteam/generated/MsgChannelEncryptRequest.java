package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgChannelEncryptRequest implements ISteamSerializableMessage {

    public static final long PROTOCOL_VERSION = 1;

    private long protocolVersion = MsgChannelEncryptRequest.PROTOCOL_VERSION;

    private EUniverse universe = EUniverse.Invalid;

    @Override
    public EMsg getEMsg() {
        return EMsg.ChannelEncryptRequest;
    }

    public long getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(long protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public EUniverse getUniverse() {
        return this.universe;
    }

    public void setUniverse(EUniverse universe) {
        this.universe = universe;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(protocolVersion);
        bw.writeInt(universe.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        protocolVersion = br.readLong();
        universe = EUniverse.from(br.readInt());
    }
}
