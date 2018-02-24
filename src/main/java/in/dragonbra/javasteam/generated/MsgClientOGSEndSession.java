package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientOGSEndSession implements ISteamSerializableMessage {

    private long sessionId = 0L;

    private int timeEnded = 0;

    private int reasonCode = 0;

    private int countAttributes = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientOGSEndSession;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public int getTimeEnded() {
        return this.timeEnded;
    }

    public void setTimeEnded(int timeEnded) {
        this.timeEnded = timeEnded;
    }

    public int getReasonCode() {
        return this.reasonCode;
    }

    public void setReasonCode(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public int getCountAttributes() {
        return this.countAttributes;
    }

    public void setCountAttributes(int countAttributes) {
        this.countAttributes = countAttributes;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(sessionId);
        bw.writeInt(timeEnded);
        bw.writeInt(reasonCode);
        bw.writeInt(countAttributes);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        sessionId = br.readLong();
        timeEnded = br.readInt();
        reasonCode = br.readInt();
        countAttributes = br.readInt();
    }
}
