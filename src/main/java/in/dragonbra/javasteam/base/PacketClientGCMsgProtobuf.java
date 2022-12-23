package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.generated.MsgGCHdrProtoBuf;
import in.dragonbra.javasteam.types.JobID;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a protobuf backed packet message.
 */
public class PacketClientGCMsgProtobuf implements IPacketGCMsg {

    private final int msgType;

    private final JobID targetJobID;

    private final JobID sourceJobID;

    private final byte[] payload;

    /**
     * Initializes a new instance of the {@link PacketClientGCMsgProtobuf} class.
     *
     * @param eMsg The network message type for this packet message.
     * @param data The data.
     */
    public PacketClientGCMsgProtobuf(int eMsg, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }

        msgType = eMsg;
        payload = data;

        MsgGCHdrProtoBuf protobufHeader = new MsgGCHdrProtoBuf();

        // we need to pull out the job ids, so we deserialize the protobuf header
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            protobufHeader.deserialize(bais);
        } catch (IOException ignored) {
        }

        targetJobID = new JobID(protobufHeader.getProto().getJobidTarget());
        sourceJobID = new JobID(protobufHeader.getProto().getJobidSource());
    }

    @Override
    public boolean isProto() {
        return true;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

    @Override
    public JobID getTargetJobID() {
        return targetJobID;
    }

    @Override
    public JobID getSourceJobID() {
        return sourceJobID;
    }

    @Override
    public byte[] getData() {
        return payload;
    }
}
