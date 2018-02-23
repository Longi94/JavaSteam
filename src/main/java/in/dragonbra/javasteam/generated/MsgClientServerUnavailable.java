package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EServerType;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientServerUnavailable implements ISteamSerializableMessage {

    private long jobidSent = 0L;

    private long eMsgSent = 0L;

    private EServerType eServerTypeUnavailable = EServerType.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientServerUnavailable;
    }

    public long getJobidSent() {
        return this.jobidSent;
    }

    public void setJobidSent(long jobidSent) {
        this.jobidSent = jobidSent;
    }

    public long getEMsgSent() {
        return this.eMsgSent;
    }

    public void setEMsgSent(long eMsgSent) {
        this.eMsgSent = eMsgSent;
    }

    public EServerType getEServerTypeUnavailable() {
        return this.eServerTypeUnavailable;
    }

    public void setEServerTypeUnavailable(EServerType eServerTypeUnavailable) {
        this.eServerTypeUnavailable = eServerTypeUnavailable;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(jobidSent);
        bw.writeLong(eMsgSent);
        bw.writeInt(eServerTypeUnavailable.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        jobidSent = br.readLong();
        eMsgSent = br.readLong();
        eServerTypeUnavailable = EServerType.from(br.readInt());
    }
}
