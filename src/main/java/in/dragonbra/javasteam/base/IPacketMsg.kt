package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;

/**
 * Represents a simple unified interface into client messages recieved from the network.
 * This is contrasted with {@link IClientMsg} in that this interface is packet body agnostic
 * and only allows simple access into the header. This interface is also immutable, and the underlying
 * data cannot be modified.
 */
public interface IPacketMsg {
    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     *
     * @return true if this instance is protobuf backed; otherwise, false
     */
    boolean isProto();

    /**
     * Gets the network message type of this packet message.
     *
     * @return The message type.
     */
    EMsg getMsgType();

    /**
     * Gets the target job id for this packet message.
     *
     * @return The target job id.
     */
    long getTargetJobID();

    /**
     * Gets the source job id for this packet message.
     *
     * @return The source job id.
     */
    long getSourceJobID();

    /**
     * Gets the underlying data that represents this client message.
     *
     * @return The data.
     */
    byte[] getData();
}
