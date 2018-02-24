package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientOGSBeginSession implements ISteamSerializableMessage {

    private byte accountType = (byte) 0;

    private long accountId = 0L;

    private int appId = 0;

    private int timeStarted = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientOGSBeginSession;
    }

    public byte getAccountType() {
        return this.accountType;
    }

    public void setAccountType(byte accountType) {
        this.accountType = accountType;
    }

    public SteamID getAccountId() {
        return new SteamID(this.accountId);
    }

    public void setAccountId(SteamID steamId) {
        this.accountId = steamId.convertToUInt64();
    }

    public int getAppId() {
        return this.appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getTimeStarted() {
        return this.timeStarted;
    }

    public void setTimeStarted(int timeStarted) {
        this.timeStarted = timeStarted;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeByte(accountType);
        bw.writeLong(accountId);
        bw.writeInt(appId);
        bw.writeInt(timeStarted);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        accountType = br.readByte();
        accountId = br.readLong();
        appId = br.readInt();
        timeStarted = br.readInt();
    }
}
