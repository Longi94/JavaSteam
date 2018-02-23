package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientOGSEndSession implements ISteamSerializableMessage {

    private long sessionId = 0L;

    private long timeEnded = 0L;

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

    public long getTimeEnded() {
        return this.timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
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
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(sessionId);
        dos.writeLong(timeEnded);
        dos.writeInt(reasonCode);
        dos.writeInt(countAttributes);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        sessionId = br.readLong();
        timeEnded = br.readLong();
        reasonCode = br.readInt();
        countAttributes = br.readInt();
    }
}
