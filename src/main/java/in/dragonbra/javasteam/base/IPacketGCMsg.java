package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.types.JobID;

/**
 * Represents a simple unified interface into game coordinator messages recieved from the network.
 * This is contrasted with {@link IClientGCMsg} in that this interface is packet body agnostic
 * and only allows simple access into the header. This interface is also immutable, and the underlying
 * data cannot be modified.
 */
public interface IPacketGCMsg {

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     *
     * @return <b>true</b> if this instance is protobuf backed; otherwise, <b>false</b>.
     */
    boolean isProto();

    /**
     * Gets the network message type of this packet message.
     *
     * @return The message type.
     */
    int getMsgType();

    /**
     * Gets the target job id for this packet message.
     *
     * @return The target job id.
     */
    JobID getTargetJobID();

    /**
     * Gets the source job id for this packet message.
     *
     * @return The source job id.
     */
    JobID getSourceJobID();

    /**
     * Gets the underlying data that represents this client message.
     *
     * @return The data.
     */
    byte[] getData();
}
