package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.types.JobID;

/**
 * Represents a unified interface into client messages.
 */
@SuppressWarnings("unused")
public interface IClientGCMsg {

    /**
     * Gets a value indicating whether this client message is protobuf backed.
     *
     * @return <b>true</b> if this instance is protobuf backed; otherwise, <b>false</b>.
     */
    boolean isProto();

    /**
     * Gets the network message type of this client message.
     *
     * @return The message type.
     */
    int getMsgType();

    /**
     * Gets the target job id for this client message.
     *
     * @return The target job id.
     */
    JobID getTargetJobID();

    /**
     * Sets the target job id for this client message.
     *
     * @param jobID The target job id.
     */
    void setTargetJobID(JobID jobID);

    /**
     * Gets the source job id for this client message.
     *
     * @return The source job id.
     */
    JobID getSourceJobID();

    /**
     * Sets the source job id for this client message.
     *
     * @param jobID The source job id.
     */
    void setSourceJobID(JobID jobID);

    /**
     * serializes this client message instance to a byte array.
     *
     * @return Data representing a client message.
     */
    byte[] serialize();

    /**
     * Initializes this client message by deserializing the specified data.
     *
     * @param data The data representing a client message.
     */
    void deserialize(byte[] data);
}
