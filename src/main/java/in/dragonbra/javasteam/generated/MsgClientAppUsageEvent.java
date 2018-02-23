package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EAppUsageEvent;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientAppUsageEvent implements ISteamSerializableMessage {

    private EAppUsageEvent appUsageEvent = EAppUsageEvent.from(0);

    private long gameID = 0L;

    private int offline = 0;

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

    public int getOffline() {
        return this.offline;
    }

    public void setOffline(int offline) {
        this.offline = offline;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(appUsageEvent.code());
        dos.writeLong(gameID);
        dos.writeInt(offline);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        appUsageEvent = EAppUsageEvent.from(br.readInt());
        gameID = br.readLong();
        offline = br.readInt();
    }
}
