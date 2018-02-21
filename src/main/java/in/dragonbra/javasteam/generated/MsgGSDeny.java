package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EDenyReason;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgGSDeny implements ISteamSerializableMessage {

    private long steamId = 0L;

    private EDenyReason denyReason = EDenyReason.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.GSDeny;
    }

    public SteamID getSteamId() {
        return new SteamID(this.steamId);
    }

    public void setSteamId(SteamID steamId) {
        this.steamId = steamId.convertToUInt64();
    }

    public EDenyReason getDenyReason() {
        return this.denyReason;
    }

    public void setDenyReason(EDenyReason denyReason) {
        this.denyReason = denyReason;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
