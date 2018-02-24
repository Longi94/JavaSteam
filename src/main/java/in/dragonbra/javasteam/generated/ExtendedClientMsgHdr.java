package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableHeader;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExtendedClientMsgHdr implements ISteamSerializableHeader {

    private EMsg msg = EMsg.Invalid;

    private byte headerSize = (byte) 36;

    private short headerVersion = (short) 2;

    private long targetJobID = Long.MAX_VALUE;

    private long sourceJobID = Long.MAX_VALUE;

    private byte headerCanary = (byte) 239;

    private long steamID = 0L;

    private int sessionID = 0;

    @Override
    public void setEMsg(EMsg msg) {
        this.msg = msg;
    }

    public EMsg getMsg() {
        return this.msg;
    }

    public void setMsg(EMsg msg) {
        this.msg = msg;
    }

    public byte getHeaderSize() {
        return this.headerSize;
    }

    public void setHeaderSize(byte headerSize) {
        this.headerSize = headerSize;
    }

    public short getHeaderVersion() {
        return this.headerVersion;
    }

    public void setHeaderVersion(short headerVersion) {
        this.headerVersion = headerVersion;
    }

    public long getTargetJobID() {
        return this.targetJobID;
    }

    public void setTargetJobID(long targetJobID) {
        this.targetJobID = targetJobID;
    }

    public long getSourceJobID() {
        return this.sourceJobID;
    }

    public void setSourceJobID(long sourceJobID) {
        this.sourceJobID = sourceJobID;
    }

    public byte getHeaderCanary() {
        return this.headerCanary;
    }

    public void setHeaderCanary(byte headerCanary) {
        this.headerCanary = headerCanary;
    }

    public SteamID getSteamID() {
        return new SteamID(this.steamID);
    }

    public void setSteamID(SteamID steamId) {
        this.steamID = steamId.convertToUInt64();
    }

    public int getSessionID() {
        return this.sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(msg.code());
        bw.writeByte(headerSize);
        bw.writeShort(headerVersion);
        bw.writeLong(targetJobID);
        bw.writeLong(sourceJobID);
        bw.writeByte(headerCanary);
        bw.writeLong(steamID);
        bw.writeInt(sessionID);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        msg = EMsg.from(br.readInt());
        headerSize = br.readByte();
        headerVersion = br.readShort();
        targetJobID = br.readLong();
        sourceJobID = br.readLong();
        headerCanary = br.readByte();
        steamID = br.readLong();
        sessionID = br.readInt();
    }
}
