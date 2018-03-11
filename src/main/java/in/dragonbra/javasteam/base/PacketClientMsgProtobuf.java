package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a protobuf backed packet message.
 */
public class PacketClientMsgProtobuf implements IPacketMsg {

    private EMsg msgType;

    private long targetJobID;

    private long sourceJobID;

    private byte[] payload;

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

        MsgHdrProtoBuf protobufHeader = new MsgHdrProtoBuf();

        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            protobufHeader.deserialize(stream);
        }

        targetJobID = protobufHeader.getProto().getJobidTarget();
        sourceJobID = protobufHeader.getProto().getJobidSource();
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
