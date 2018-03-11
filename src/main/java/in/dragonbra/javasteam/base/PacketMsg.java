package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a packet message with basic header information.
 */
public class PacketMsg implements IPacketMsg {

    private EMsg msgType;

    private long targetJobID;

    private long sourceJobID;

    private byte[] payload;

    /**
     * Initializes a new instance of the{@link PacketMsg} class.
     *
     * @param eMsg The network message type for this packet message.
     * @param data The data.
     * @throws IOException exception while deserializing the data
     */
    public PacketMsg(EMsg eMsg, byte[] data) throws IOException {
        this.msgType = eMsg;
        this.payload = data;

        MsgHdr msgHdr = new MsgHdr();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            msgHdr.deserialize(stream);
        }

        targetJobID = msgHdr.getTargetJobID();
        sourceJobID = msgHdr.getSourceJobID();
    }

    @Override
    public boolean isProto() {
        return false;
    }

    @Override
    public EMsg getMsgType() {
        return this.msgType;
    }

    @Override
    public long getTargetJobID() {
        return this.targetJobID;
    }

    @Override
    public long getSourceJobID() {
        return this.sourceJobID;
    }

    @Override
    public byte[] getData() {
        return payload;
    }
}
