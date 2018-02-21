package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientOGSWriteRow implements ISteamSerializableMessage {

    private long sessionId = 0L;

    private int countAttributes = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientOGSWriteRow;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
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
