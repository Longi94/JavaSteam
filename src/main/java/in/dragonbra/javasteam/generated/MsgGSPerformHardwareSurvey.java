package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

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
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
