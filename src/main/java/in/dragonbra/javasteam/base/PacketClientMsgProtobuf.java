package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a protobuf backed packet message.
 */
public class PacketClientMsgProtobuf implements IPacketMsg {

    private final EMsg msgType;

    private final byte[] payload;

    private final MsgHdrProtoBuf header;

    /**
     * Initializes a new instance of the {@link PacketClientMsgProtobuf} class.
     *
     * @param eMsg The network message type for this packet message.
     * @param data The data.
     * @throws IOException exception while deserializing the data
     */
    public PacketClientMsgProtobuf(EMsg eMsg, byte[] data) throws IOException {
        this.msgType = eMsg;
        this.payload = data;

        header = new MsgHdrProtoBuf();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            header.deserialize(stream);
        }
    }

    /**
     * Gets the header for this packet message.
     *
     * @return The header.
     */
    public MsgHdrProtoBuf getHeader() {
        return header;
    }

    @Override
    public boolean isProto() {
        return true;
    }

    @Override
    public EMsg getMsgType() {
        return msgType;
    }

    @Override
    public long getTargetJobID() {
        return header.getProto().getJobidTarget();
    }

    @Override
    public long getSourceJobID() {
        return header.getProto().getJobidSource();
    }

    @Override
    public byte[] getData() {
        return payload;
    }
}
