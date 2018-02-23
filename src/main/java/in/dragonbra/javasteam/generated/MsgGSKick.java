package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EDenyReason;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgGSKick implements ISteamSerializableMessage {

    private long steamId = 0L;

    private EDenyReason denyReason = EDenyReason.from(0);

    private int waitTilMapChange = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSKick;
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

    public int getWaitTilMapChange() {
        return this.waitTilMapChange;
    }

    public void setWaitTilMapChange(int waitTilMapChange) {
        this.waitTilMapChange = waitTilMapChange;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(steamId);
        dos.writeInt(denyReason.code());
        dos.writeInt(waitTilMapChange);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        steamId = br.readLong();
        denyReason = EDenyReason.from(br.readInt());
        waitTilMapChange = br.readInt();
    }
}
