package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

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
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
