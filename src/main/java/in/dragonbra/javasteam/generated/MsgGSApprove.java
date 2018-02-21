package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgGSApprove implements ISteamSerializableMessage {

    private long steamId = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSApprove;
    }

    public SteamID getSteamId() {
        return new SteamID(this.steamId);
    }

    public void setSteamId(SteamID steamId) {
        this.steamId = steamId.convertToUInt64();
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
