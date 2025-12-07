package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.MsgHdrProtoBuf
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException
import kotlin.jvm.Throws

/**
 * Represents a protobuf backed packet message.
 * @constructor Initializes a new instance of the [PacketClientMsgProtobuf] class.
 * @param eMsg The network message type for this packet message.
 * @param data The data.
 */
class PacketClientMsgProtobuf
@Throws(IOException::class)
constructor(
    private val eMsg: EMsg,
    data: ByteArray,
) : IPacketMsg {

    private val payload: ByteArray = data

    /**
     * Gets the header for this packet message.
     */
    internal val header: MsgHdrProtoBuf = MsgHdrProtoBuf()

    /**
     * Gets the offset in payload to the body after the header.
     */
    internal val bodyOffset: Long

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     * This type of message is always protobuf backed.
     */
    override val isProto: Boolean
        get() = true

    /**
     * Gets the network message type of this packet message.
     */
    override val msgType: EMsg
        get() = eMsg

    /**
     * Gets the target job id for this packet message.
     */
    override val targetJobID: Long
        get() = header.proto.jobidTarget

    /**
     * Gets the source job id for this packet message.
     */
    override val sourceJobID: Long
        get() = header.proto.jobidSource

    /**
     * Gets the underlying data that represents this client message.
     */
    override val data: ByteArray
        get() = payload

    init {
        // we need to pull out the job ids, so we deserialize the protobuf header
        MemoryStream(data).use { ms ->
            header.deserialize(ms)
            bodyOffset = ms.position
        }
    }
}
