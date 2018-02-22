package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.ExtendedClientMsgHdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a packet message with extended header information.
 */
public class PacketClientMsg implements IPacketMsg {

    private EMsg msgType;

    private long targetJobID;

    private long sourceJobID;

    private byte[] payload;

    /**
     * Initializes a new instance of the {@link PacketClientMsg} class.
     *
     * @param eMsg The network message type for this packet message.
     * @param data The data.
     * @throws IOException
     */
    public PacketClientMsg(EMsg eMsg, byte[] data) throws IOException {
        this.msgType = eMsg;
        this.payload = data;

        ExtendedClientMsgHdr extendedHdr = new ExtendedClientMsgHdr();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            extendedHdr.deserialize(stream);
        }

        targetJobID = extendedHdr.getTargetJobID();
        sourceJobID = extendedHdr.getSourceJobID();
    }

    @Override
    public boolean isProto() {
        return false;
    }

    @Override
    public EMsg getMsgType() {
        return msgType;
    }

    @Override
    public long getTargetJobID() {
        return targetJobID;
    }

    @Override
    public long getSourceJobID() {
        return sourceJobID;
    }

    @Override
    public byte[] getData() {
        return payload;
    }
}
