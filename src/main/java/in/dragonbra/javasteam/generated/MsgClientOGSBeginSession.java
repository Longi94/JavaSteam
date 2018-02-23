package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientOGSBeginSession implements ISteamSerializableMessage {

    private byte accountType = (byte) 0;

    private long accountId = 0L;

    private long appId = 0L;

    private long timeStarted = 0L;

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

    public long getAppId() {
        return this.appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getTimeStarted() {
        return this.timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeByte(accountType);
        dos.writeLong(accountId);
        dos.writeLong(appId);
        dos.writeLong(timeStarted);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        accountType = br.readByte();
        accountId = br.readLong();
        appId = br.readLong();
        timeStarted = br.readLong();
    }
}
