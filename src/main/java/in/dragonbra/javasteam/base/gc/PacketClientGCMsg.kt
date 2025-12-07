package `in`.dragonbra.javasteam.base.gc

import `in`.dragonbra.javasteam.generated.MsgGCHdr
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException

/**
 * Represents a packet message with extended header information.
 * @constructor Initializes a new instance of the [PacketClientGCMsg] class.
 * @param eMsg The network message type for this packet message.
 * @param data The data.
 */
class PacketClientGCMsg(private val eMsg: Int, data: ByteArray) : IPacketGCMsg {

    private val gcHdr: MsgGCHdr = MsgGCHdr()

    private val payload: ByteArray = data

    /**
     * Gets a value indicating whether this packet message is protobuf backed.
     * This type of message is never protobuf backed.
     */
    override val isProto: Boolean
        get() = false

    /**
     *  Gets the network message type of this packet message.
     */
    override val msgType: Int
        get() = eMsg

    /**
     * Gets the target job id for this packet message.
     */
    override val targetJobID: JobID
        get() = JobID(gcHdr.targetJobID)

    /**
     * Gets the source job id for this packet message.
     */
    override val sourceJobID: JobID
        get() = JobID(gcHdr.sourceJobID)

    /**
     * Gets the underlying data that represents this packet message.
     */
    override val data: ByteArray
        get() = payload

    init {
        // deserialize the gc header to get our hands on the job ids
        try {
            MemoryStream(data).use { ms ->
                gcHdr.deserialize(ms)
            }
        } catch (_: IOException) {
        }
    }
}
