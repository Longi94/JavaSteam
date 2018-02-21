package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;

import java.io.*;

public class MsgGSGetPlayStatsResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private int rank = 0;

    private long lifetimeConnects = 0L;

    private long lifetimeMinutesPlayed = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSGetPlayStatsResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public int getRank() {
        return this.rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getLifetimeConnects() {
        return this.lifetimeConnects;
    }

    public void setLifetimeConnects(long lifetimeConnects) {
        this.lifetimeConnects = lifetimeConnects;
    }

    public long getLifetimeMinutesPlayed() {
        return this.lifetimeMinutesPlayed;
    }

    public void setLifetimeMinutesPlayed(long lifetimeMinutesPlayed) {
        this.lifetimeMinutesPlayed = lifetimeMinutesPlayed;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeInt(rank);
        dos.writeLong(lifetimeConnects);
        dos.writeLong(lifetimeMinutesPlayed);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        result = EResult.from(dis.readInt());
        rank = dis.readInt();
        lifetimeConnects = dis.readLong();
        lifetimeMinutesPlayed = dis.readLong();
    }
}
