package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientNewLoginKeyAccepted implements ISteamSerializableMessage {

    private long uniqueID = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientNewLoginKeyAccepted;
    }

    public long getUniqueID() {
        return this.uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
