package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.MsgHdrProtoBuf
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.IOException

/**
 * Represents a protobuf backed client message. Only contains the header information.
 */
@Suppress("unused")
open class AClientMsgProtobuf : MsgBase<MsgHdrProtoBuf> {

    companion object {
        private val logger: Logger = LogManager.getLogger(AClientMsgProtobuf::class.java)
    }

    /**
     * Shorthand accessor for the protobuf header.
     */
    val protoHeader: SteammessagesBase.CMsgProtoBufHeader.Builder
        get() = header.proto

    /**
     * Gets a value indicating whether this client message is protobuf backed.
     * Client messages of this type are always protobuf backed.
     * @return **true** if this instance is protobuf backed; otherwise, **false**.
     */
    override val isProto: Boolean
        get() = true

    /**
     * Gets the network message type of this client message.
     * @return The network message type.
     */
    override val msgType: EMsg
        get() = header.msg

    /**
     * Gets or Sets the session id for this client message.
     */
    override var sessionID: Int
        get() = protoHeader.clientSessionid
        set(value) {
            protoHeader.clientSessionid = value
        }

    /**
     * Gets or Sets the [SteamID] for this client message.
     */
    override var steamID: SteamID?
        get() = SteamID(protoHeader.steamid)
        set(value) {
            protoHeader.steamid = value!!.convertToUInt64()
        }

    /**
     * Gets or Sets the target job id for this client message.
     */
    override var targetJobID: JobID
        get() = JobID(protoHeader.jobidTarget)
        set(value) {
            protoHeader.jobidTarget = value.value
        }

    /**
     * Gets or Sets the source job id for this client message.
     */
    override var sourceJobID: JobID
        get() = JobID(protoHeader.jobidSource)
        set(value) {
            protoHeader.jobidSource = value.value
        }

    /**
     * Initializes a new instance of the [AClientMsgProtobuf] class.
     * This is a recieve constructor.
     * @param msg The packet message to build this client message from.
     */
    constructor(msg: IPacketMsg) : this(msg.msgType) {
        if (!msg.isProto) {
            logger.error("ClientMsgProtobuf used for non-proto message!")
        }

        deserialize(msg.data)
    }

    private constructor() : this(0)

    internal constructor(payloadReserve: Int) : super(MsgHdrProtoBuf::class.java, payloadReserve)

    private constructor(eMsg: EMsg, payloadReserve: Int = 0) : super(MsgHdrProtoBuf::class.java, payloadReserve) {
        // set our emsg
        header.setEMsg(eMsg)
    }

    /**
     * Serializes this client message instance to a byte array.
     * @throws UnsupportedOperationException This class is for reading Protobuf messages only. If you want to create a protobuf message, use [ClientMsgProtobuf].
     */
    override fun serialize(): ByteArray = throw UnsupportedOperationException("ClientMsgProtobuf is for reading only. Use ClientMsgProtobuf<T> for serializing messages.")

    /**
     * Initializes this client message by deserializing the specified data.
     * @param data The data representing a client message.
     */
    override fun deserialize(data: ByteArray) {
        try {
            MemoryStream(data).use { ms ->
                header.deserialize(ms)
            }
        } catch (e: IOException) {
            logger.error(e)
        }
    }
}
