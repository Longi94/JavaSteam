package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.enums.EUdpPacketType;

public class UdpHeader {

    public static final int MAGIC = 0x31305356;

    private int magic = UdpHeader.MAGIC;

    private short payloadSize = (short) 0;

    private EUdpPacketType packetType = EUdpPacketType.Invalid;

    private byte flags = (byte) 0;

    private int sourceConnID = 512;

    private int destConnID = 0;

    private int seqThis = 0;

    private int seqAck = 0;

    private int packetsInMsg = 0;

    private int msgStartSeq = 0;

    private int msgSize = 0;

    public int getMagic() {
        return this.magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public short getPayloadSize() {
        return this.payloadSize;
    }

    public void setPayloadSize(short payloadSize) {
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

    public int getSourceConnID() {
        return this.sourceConnID;
    }

    public void setSourceConnID(int sourceConnID) {
        this.sourceConnID = sourceConnID;
    }

    public int getDestConnID() {
        return this.destConnID;
    }

    public void setDestConnID(int destConnID) {
        this.destConnID = destConnID;
    }

    public int getSeqThis() {
        return this.seqThis;
    }

    public void setSeqThis(int seqThis) {
        this.seqThis = seqThis;
    }

    public int getSeqAck() {
        return this.seqAck;
    }

    public void setSeqAck(int seqAck) {
        this.seqAck = seqAck;
    }

    public int getPacketsInMsg() {
        return this.packetsInMsg;
    }

    public void setPacketsInMsg(int packetsInMsg) {
        this.packetsInMsg = packetsInMsg;
    }

    public int getMsgStartSeq() {
        return this.msgStartSeq;
    }

    public void setMsgStartSeq(int msgStartSeq) {
        this.msgStartSeq = msgStartSeq;
    }

    public int getMsgSize() {
        return this.msgSize;
    }

    public void setMsgSize(int msgSize) {
        this.msgSize = msgSize;
    }

}
