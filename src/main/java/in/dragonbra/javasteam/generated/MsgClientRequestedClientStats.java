package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientRequestedClientStats implements ISteamSerializableMessage {

    private int countStats = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientRequestedClientStats;
    }

    public int getCountStats() {
        return this.countStats;
    }

    public void setCountStats(int countStats) {
        this.countStats = countStats;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
