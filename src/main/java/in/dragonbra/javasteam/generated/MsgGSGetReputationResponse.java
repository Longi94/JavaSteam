package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgGSGetReputationResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private int reputationScore = 0;

    private boolean banned = false;

    private int bannedIp = 0;

    private short bannedPort = (short) 0;

    private long bannedGameId = 0L;

    private int timeBanExpires = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSGetReputationResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public int getReputationScore() {
        return this.reputationScore;
    }

    public void setReputationScore(int reputationScore) {
        this.reputationScore = reputationScore;
    }

    public boolean getBanned() {
        return this.banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getBannedIp() {
        return this.bannedIp;
    }

    public void setBannedIp(int bannedIp) {
        this.bannedIp = bannedIp;
    }

    public short getBannedPort() {
        return this.bannedPort;
    }

    public void setBannedPort(short bannedPort) {
        this.bannedPort = bannedPort;
    }

    public long getBannedGameId() {
        return this.bannedGameId;
    }

    public void setBannedGameId(long bannedGameId) {
        this.bannedGameId = bannedGameId;
    }

    public int getTimeBanExpires() {
        return this.timeBanExpires;
    }

    public void setTimeBanExpires(int timeBanExpires) {
        this.timeBanExpires = timeBanExpires;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(result.code());
        bw.writeInt(reputationScore);
        bw.writeBoolean(banned);
        bw.writeInt(bannedIp);
        bw.writeShort(bannedPort);
        bw.writeLong(bannedGameId);
        bw.writeInt(timeBanExpires);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
        reputationScore = br.readInt();
        banned = br.readBoolean();
        bannedIp = br.readInt();
        bannedPort = br.readShort();
        bannedGameId = br.readLong();
        timeBanExpires = br.readInt();
    }
}
