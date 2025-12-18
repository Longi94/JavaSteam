package `in`.dragonbra.javasteam.base.gc

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.generated.MsgGCHdrProtoBuf
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import java.io.IOException
import java.lang.reflect.InvocationTargetException

/**
 * Represents a protobuf backed game coordinator message.
 * @constructor Initializes a new instance of the [ClientGCMsgProtobuf] class.
 * This is a client send constructor.
 * @param clazz The type of the body.
 * @param eMsg The network message type this client message represents.
 * @param payloadReserve The number of bytes to initialize the payload capacity to.
 */
@Suppress("unused")
class ClientGCMsgProtobuf<BodyType : GeneratedMessage.Builder<BodyType>> @JvmOverloads constructor(
    private val clazz: Class<out AbstractMessage>,
    eMsg: Int,
    payloadReserve: Int = 64,
) : GCMsgBase<MsgGCHdrProtoBuf>(MsgGCHdrProtoBuf::class.java, payloadReserve) {

    companion object {
        private val logger: Logger = LogManager.getLogger(ClientGCMsgProtobuf::class.java)
    }

    /**
     * Gets the body structure of this message.
     */
    lateinit var body: BodyType
        private set

    /**
     * Shorthand accessor for the protobuf header.
     */
    val protoHeader: SteammessagesBase.CMsgProtoBufHeader.Builder
        get() = header.proto

    /**
     * Gets a value indicating whether this gc message is protobuf backed.
     * Client messages of this type are always protobuf backed.
     * @return **true** if this instance is protobuf backed; otherwise, **false**
     */
    override val isProto: Boolean
        get() = true

    /**
     * Gets the network message type of this gc message.
     * @return The network message type.
     */
    override val msgType: Int
        get() = header.msg

    /**
     * Gets or sets the target job id for this gc message.
     */
    override var targetJobID: JobID
        get() = JobID(protoHeader.jobidTarget)
        set(value) {
            protoHeader.jobidTarget = value.value
        }

    /**
     * Gets or sets the source job id for this gc message.
     */
    override var sourceJobID: JobID
        get() = JobID(protoHeader.jobidSource)
        set(value) {
            protoHeader.jobidSource = value.value
        }

    /**
     * Initializes a new instance of the [ClientGCMsgProtobuf] class.
     * This is a client send constructor.
     * @param clazz The type of the body.
     * @param msg The network message type this client message represents.
     */
    constructor(clazz: Class<out AbstractMessage>, msg: IPacketGCMsg) : this(clazz, msg.msgType) {
        if (!msg.isProto) {
            logger.debug("ClientMsgProtobuf<${clazz.simpleName}> used for non-proto message!")
        }

        deserialize(msg.data)
    }

    /**
     * Initializes a new instance of the [ClientGCMsgProtobuf] class.
     * This is a reply constructor.
     * @param clazz The type of the body.
     * @param eMsg The network message type this client message represents.
     * @param msg  The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    @JvmOverloads
    constructor(
        clazz: Class<out AbstractMessage>,
        eMsg: Int,
        msg: GCMsgBase<MsgGCHdrProtoBuf>,
        payloadReserve: Int = 64,
    ) : this(clazz, eMsg, payloadReserve) {
        // our target is where the message came from
        header.proto.setJobidTarget(msg.header.proto.jobidSource)
    }

    init {
        try {
            val m = clazz.getMethod("newBuilder")
            @Suppress("UNCHECKED_CAST")
            body = m.invoke(null) as BodyType
        } catch (e: IllegalAccessException) {
            logger.error(e)
        } catch (e: NoSuchMethodException) {
            logger.error(e)
        } catch (e: InvocationTargetException) {
            logger.error(e)
        }

        header.setEMsg(eMsg)
    }

    /**
     * Serializes this gc message instance to a byte array.
     * @return The data representing a gc message.
     */
    override fun serialize(): ByteArray {
        try {
            MemoryStream().use { ms ->
                val os = ms.asOutputStream()
                header.serialize(os)
                body.build().writeTo(os)
                os.write(payload.toByteArray())
                return ms.toByteArray()
            }
        } catch (_: IOException) {
        }

        return ByteArray(0)
    }

    /**
     * Initializes this gc message by deserializing the specified data.
     * @param data The data representing a gc message.
     */
    override fun deserialize(data: ByteArray) {
        try {
            MemoryStream(data).use { ms ->
                header.deserialize(ms)
                val m = clazz.getMethod("newBuilder")
                @Suppress("UNCHECKED_CAST")
                body = m.invoke(null) as BodyType
                body.mergeFrom(ms)
                payload.write(data, ms.position.toInt(), ms.available())
                payload.seek(0, SeekOrigin.BEGIN)
            }
        } catch (e: IOException) {
            logger.error(e)
        } catch (e: IllegalAccessException) {
            logger.error(e)
        } catch (e: NoSuchMethodException) {
            logger.error(e)
        } catch (e: InvocationTargetException) {
            logger.error(e)
        }
    }
}
