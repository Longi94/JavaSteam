package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.ExtendedClientMsgHdr
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException

/**
 * Represents a packet message with extended header information.
 * @constructor Initializes a new instance of the [PacketClientMsg] class.
 * @param eMsg The network message type for this packet message.
 * @param data The data.
 * @throws IOException exception while deserializing the data
 */
class PacketClientMsg
@Throws(IOException::class)
constructor(
    private val eMsg: EMsg,
    data: ByteArray,
) : IPacketMsg {

    private val payload: ByteArray = data

    /**
     * Gets the header for this packet message.
     */
    internal val header: ExtendedClientMsgHdr = ExtendedClientMsgHdr()

    /**
     * The offset in payload after the header.
     */
    internal val bodyOffset: Long

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     * This type of message is never protobuf backed.
     */
    override val isProto: Boolean
        get() = false

    /**
     * Gets the network message type of this packet message.
     */
    override val msgType: EMsg
        get() = eMsg

    /**
     * Gets the target job id for this packet message.
     */
    override val targetJobID: Long
        get() = header.targetJobID

    /**
     * Gets the source job id for this packet message.
     */
    override val sourceJobID: Long
        get() = header.sourceJobID

    /**
     * Gets the underlying data that represents this client message.
     */
    override val data: ByteArray
        get() = payload

    init {
        // deserialize the extended header to get our hands on the job ids
        MemoryStream(data).use { ms ->
            header.deserialize(ms)
            bodyOffset = ms.position
        }
    }
}
