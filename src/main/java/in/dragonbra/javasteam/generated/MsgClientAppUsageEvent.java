package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EAppUsageEvent;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientAppUsageEvent implements ISteamSerializableMessage {

    private EAppUsageEvent appUsageEvent = EAppUsageEvent.from(0);

    private long gameID = 0L;

    private short offline = (short) 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientAppUsageEvent;
    }

    public EAppUsageEvent getAppUsageEvent() {
        return this.appUsageEvent;
    }

    public void setAppUsageEvent(EAppUsageEvent appUsageEvent) {
        this.appUsageEvent = appUsageEvent;
    }

    public GameID getGameID() {
        return new GameID(this.gameID);
    }

    public void setGameID(GameID gameId) {
        this.gameID = gameId.convertToUInt64();
    }

    public short getOffline() {
        return this.offline;
    }

    public void setOffline(short offline) {
        this.offline = offline;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(appUsageEvent.code());
        bw.writeLong(gameID);
        bw.writeShort(offline);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        appUsageEvent = EAppUsageEvent.from(br.readInt());
        gameID = br.readLong();
        offline = br.readShort();
    }
}
