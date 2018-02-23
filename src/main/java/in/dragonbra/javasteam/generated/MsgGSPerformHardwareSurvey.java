package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgGSPerformHardwareSurvey implements ISteamSerializableMessage {

    private long flags = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSPerformHardwareSurvey;
    }

    public long getFlags() {
        return this.flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(flags);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        flags = br.readLong();
    }
}
