package `in`.dragonbra.javasteam.base.gc

import `in`.dragonbra.javasteam.generated.MsgGCHdrProtoBuf
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException

/**
 * Represents a protobuf backed packet message.
 * @constructor Initializes a new instance of the [PacketClientGCMsgProtobuf] class.
 * @param eMsg The network message type for this packet message.
 * @param data The data.
 */
class PacketClientGCMsgProtobuf(private val eMsg: Int, data: ByteArray) : IPacketGCMsg {

    private val protobufHeader: MsgGCHdrProtoBuf = MsgGCHdrProtoBuf()

    private val payload: ByteArray = data

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     * This type of message is always protobuf backed.
     */
    override val isProto: Boolean
        get() = true

    /**
     * Gets the network message type of this packet message.
     */
    override val msgType: Int
        get() = eMsg

    /**
     * Gets the target job id for this packet message.
     */
    override val targetJobID: JobID
        get() = JobID(protobufHeader.proto.jobidTarget)

    /**
     * Gets the source job id for this packet message.
     */
    override val sourceJobID: JobID
        get() = JobID(protobufHeader.proto.jobidSource)

    /**
     * Gets the underlying data that represents this client message.
     */
    override val data: ByteArray
        get() = payload

    init {
        // we need to pull out the job ids, so we deserialize the protobuf header
        try {
            MemoryStream(data).use { ms ->
                protobufHeader.deserialize(ms)
            }
        } catch (_: IOException) {
        }
    }
}
