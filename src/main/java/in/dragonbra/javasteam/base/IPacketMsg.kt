package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.enums.EMsg

/**
 * Represents a simple unified interface into client messages recieved from the network.
 * This is contrasted with [IClientMsg] in that this interface is packet body agnostic
 * and only allows simple access into the header. This interface is also immutable, and the underlying
 * data cannot be modified.
 */
interface IPacketMsg {

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     * @return true if this instance is protobuf backed; otherwise, false
     */
    val isProto: Boolean

    /**
     * Gets the network message type of this packet message.
     * @return The message type.
     */
    val msgType: EMsg

    /**
     * Gets the target job id for this packet message.
     * @return The target job id.
     */
    val targetJobID: Long

    /**
     * Gets the source job id for this packet message.
     * @return The source job id.
     */
    val sourceJobID: Long

    /**
     * Gets the underlying data that represents this client message.
     * @return The data.
     */
    val data: ByteArray
}
