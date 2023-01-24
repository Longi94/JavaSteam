package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.generated.MsgGCHdr;
import in.dragonbra.javasteam.types.JobID;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a packet message with extended header information.
 */
public class PacketClientGCMsg implements IPacketGCMsg {

    private final int msgType;

    private final JobID targetJobID;

    private final JobID sourceJobID;

    private final byte[] payload;

    /**
     * Initializes a new instance of the {@link PacketClientGCMsg} class.
     *
     * @param eMsg The network message type for this packet message.
     * @param data The data.
     */
    public PacketClientGCMsg(int eMsg, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }

        msgType = eMsg;
        payload = data;

        MsgGCHdr gcHdr = new MsgGCHdr();

        // we need to pull out the job ids, so we deserialize the protobuf header
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            gcHdr.deserialize(bais);
        } catch (IOException ignored) {
        }

        targetJobID = new JobID(gcHdr.getTargetJobID());
        sourceJobID = new JobID(gcHdr.getSourceJobID());
    }

    @Override
    public boolean isProto() {
        return false;
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
