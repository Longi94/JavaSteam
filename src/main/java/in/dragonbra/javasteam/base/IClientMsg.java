package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;

import java.io.IOException;

/**
 * Represents a unified interface into client messages.
 */
public interface IClientMsg {

    /**
     * Gets a value indicating whether this client message is protobuf backed.
     *
     * @return <c>true</c> if this instance is protobuf backed; otherwise, <c>false</c>.
     */
    boolean isProto();

    /**
     * Gets the network message type of this client message.
     *
     * @return The message type.
     */
    EMsg getMsgType();

    /**
     * Gets the session id for this client message.
     *
     * @return The session id.
     */
    int getSessionID();

    /**
     * Sets the session id for this client message.
     *
     * @param sessionID The session id.
     */
    void setSessionID(int sessionID);

    /**
     * Gets the {@link SteamID} for this client message.
     *
     * @return The {@link SteamID}.
     */
    SteamID getSteamID();

    /**
     * Sets the {@link SteamID} for this client message.
     *
     * @param SteamID The {@link SteamID}.
     */
    void setSteamID(SteamID SteamID);

    /**
     * Gets the target job id for this client message.
     *
     * @return The target job id.
     */
    JobID getTargetJobID();

    /**
     * Sets the target job id for this client message.
     *
     * @param JobID The target job id.
     */
    void setTargetJobID(JobID JobID);

    /**
     * Gets the source job id for this client message.
     *
     * @return The source job id.
     */
    JobID getSourceJobID();

    /**
     * Sets the source job id for this client message.
     *
     * @param JobID The source job id.
     */
    void setSourceJobID(JobID JobID);

    /**
     * serializes this client message instance to a byte array.
     *
     * @return Data representing a client message.
     * @throws IOException
     */
    byte[] serialize() throws IOException;

    /**
     * Initializes this client message by deserializing the specified data.
     *
     * @param data The data representing a client message.
     */
    void deSerialize(byte[] data) throws IOException;
}
