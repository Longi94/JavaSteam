package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.enums.EUdpPacketType;

public class UdpHeader {

    public static final long MAGIC = 0x31305356;

    private long magic = UdpHeader.MAGIC;

    private int payloadSize = 0;

    private EUdpPacketType packetType = EUdpPacketType.Invalid;

    private byte flags = (byte) 0;

    private long sourceConnID = 512L;

    private long destConnID = 0L;

    private long seqThis = 0L;

    private long seqAck = 0L;

    private long packetsInMsg = 0L;

    private long msgStartSeq = 0L;

    private long msgSize = 0L;

    public long getMagic() {
        return this.magic;
    }

    public void setMagic(long magic) {
        this.magic = magic;
    }

    public int getPayloadSize() {
        return this.payloadSize;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    public EUdpPacketType getPacketType() {
        return this.packetType;
    }

    public void setPacketType(EUdpPacketType packetType) {
        this.packetType = packetType;
    }

    public byte getFlags() {
        return this.flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public long getSourceConnID() {
        return this.sourceConnID;
    }

    public void setSourceConnID(long sourceConnID) {
        this.sourceConnID = sourceConnID;
    }

    public long getDestConnID() {
        return this.destConnID;
    }

    public void setDestConnID(long destConnID) {
        this.destConnID = destConnID;
    }

    public long getSeqThis() {
        return this.seqThis;
    }

    public void setSeqThis(long seqThis) {
        this.seqThis = seqThis;
    }

    public long getSeqAck() {
        return this.seqAck;
    }

    public void setSeqAck(long seqAck) {
        this.seqAck = seqAck;
    }

    public long getPacketsInMsg() {
        return this.packetsInMsg;
    }

    public void setPacketsInMsg(long packetsInMsg) {
        this.packetsInMsg = packetsInMsg;
    }

    public long getMsgStartSeq() {
        return this.msgStartSeq;
    }

    public void setMsgStartSeq(long msgStartSeq) {
        this.msgStartSeq = msgStartSeq;
    }

    public long getMsgSize() {
        return this.msgSize;
    }

    public void setMsgSize(long msgSize) {
        this.msgSize = msgSize;
    }

}
