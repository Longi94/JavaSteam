package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientVACBanStatus implements ISteamSerializableMessage {

    private long numBans = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientVACBanStatus;
    }

    public long getNumBans() {
        return this.numBans;
    }

    public void setNumBans(long numBans) {
        this.numBans = numBans;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
