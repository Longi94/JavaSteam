package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientLogOnResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private int outOfGameHeartbeatRateSec = 0;

    private int inGameHeartbeatRateSec = 0;

    private long clientSuppliedSteamId = 0L;

    private int ipPublic = 0;

    private int serverRealTime = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientLogOnResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public int getOutOfGameHeartbeatRateSec() {
        return this.outOfGameHeartbeatRateSec;
    }

    public void setOutOfGameHeartbeatRateSec(int outOfGameHeartbeatRateSec) {
        this.outOfGameHeartbeatRateSec = outOfGameHeartbeatRateSec;
    }

    public int getInGameHeartbeatRateSec() {
        return this.inGameHeartbeatRateSec;
    }

    public void setInGameHeartbeatRateSec(int inGameHeartbeatRateSec) {
        this.inGameHeartbeatRateSec = inGameHeartbeatRateSec;
    }

    public SteamID getClientSuppliedSteamId() {
        return new SteamID(this.clientSuppliedSteamId);
    }

    public void setClientSuppliedSteamId(SteamID steamId) {
        this.clientSuppliedSteamId = steamId.convertToUInt64();
    }

    public int getIpPublic() {
        return this.ipPublic;
    }

    public void setIpPublic(int ipPublic) {
        this.ipPublic = ipPublic;
    }

    public int getServerRealTime() {
        return this.serverRealTime;
    }

    public void setServerRealTime(int serverRealTime) {
        this.serverRealTime = serverRealTime;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(result.code());
        bw.writeInt(outOfGameHeartbeatRateSec);
        bw.writeInt(inGameHeartbeatRateSec);
        bw.writeLong(clientSuppliedSteamId);
        bw.writeInt(ipPublic);
        bw.writeInt(serverRealTime);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
        outOfGameHeartbeatRateSec = br.readInt();
        inGameHeartbeatRateSec = br.readInt();
        clientSuppliedSteamId = br.readLong();
        ipPublic = br.readInt();
        serverRealTime = br.readInt();
    }
}
