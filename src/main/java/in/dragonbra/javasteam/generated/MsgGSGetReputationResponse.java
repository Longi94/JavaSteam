package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgGSGetReputationResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private long reputationScore = 0L;

    private boolean banned = false;

    private long bannedIp = 0L;

    private int bannedPort = 0;

    private long bannedGameId = 0L;

    private long timeBanExpires = 0L;

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

    public long getReputationScore() {
        return this.reputationScore;
    }

    public void setReputationScore(long reputationScore) {
        this.reputationScore = reputationScore;
    }

    public boolean getBanned() {
        return this.banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public long getBannedIp() {
        return this.bannedIp;
    }

    public void setBannedIp(long bannedIp) {
        this.bannedIp = bannedIp;
    }

    public int getBannedPort() {
        return this.bannedPort;
    }

    public void setBannedPort(int bannedPort) {
        this.bannedPort = bannedPort;
    }

    public long getBannedGameId() {
        return this.bannedGameId;
    }

    public void setBannedGameId(long bannedGameId) {
        this.bannedGameId = bannedGameId;
    }

    public long getTimeBanExpires() {
        return this.timeBanExpires;
    }

    public void setTimeBanExpires(long timeBanExpires) {
        this.timeBanExpires = timeBanExpires;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
