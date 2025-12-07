package `in`.dragonbra.javasteam.base.gc

import `in`.dragonbra.javasteam.generated.MsgGCHdr
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException

/**
 * Represents a struct backed game coordinator message.
 * @constructor Initializes a new instance of the [ClientGCMsg] class.
 * @param bodyType The body type.
 * @param payloadReserve The number of bytes to initialize the payload capacity to.
 */
@Suppress("unused")
class ClientGCMsg<BodyType : IGCSerializableMessage> @JvmOverloads constructor(
    bodyType: Class<out BodyType>,
    payloadReserve: Int = 64,
) : GCMsgBase<MsgGCHdr>(MsgGCHdr::class.java, payloadReserve) {

    companion object {
        private val logger: Logger = LogManager.getLogger(ClientGCMsg::class.java)
    }

    /**
     * Gets the body structure of this message.
     */
    lateinit var body: BodyType

    /**
     * Gets a value indicating whether this gc message is protobuf backed.
     * @return **true** if this instance is protobuf backed; otherwise, **false**
     */
    override val isProto: Boolean
        get() = false

    /**
     * Gets the network message type of this gc message.
     * @return The network message type.
     */
    override val msgType: Int
        get() = body.eMsg

    /**
     * Gets or Sets the target job id for this gc message.
     */
    override var targetJobID: JobID
        get() = JobID(header.targetJobID)
        set(value) {
            header.targetJobID = value.value
        }

    /**
     * Gets or Sets the source job id for this gc message.
     */
    override var sourceJobID: JobID
        get() = JobID(header.sourceJobID)
        set(value) {
            header.sourceJobID = value.value
        }

    /**
     * Initializes a new instance of the [ClientGCMsg] class.
     * This a reply constructor.
     * @param bodyType The body type.
     * @param msg The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    @JvmOverloads
    constructor(bodyType: Class<out BodyType>, msg: GCMsgBase<MsgGCHdr>, payloadReserve: Int = 64) :
        this(bodyType, payloadReserve) {
        // our target is where the message came from
        header.targetJobID = msg.header.sourceJobID
    }

    /**
     * Initializes a new instance of the [ClientGCMsg] class.
     * This a receive constructor.
     * @param bodyType The body type.
     * @param msg The packet message to build this client message from.
     */
    constructor(bodyType: Class<out BodyType>, msg: IPacketGCMsg) : this(bodyType) {
        if (msg.isProto) {
            logger.error("ClientMsg<${bodyType.simpleName}> used for proto message!")
        }

        deserialize(msg.data)
    }

    init {
        try {
            body = bodyType.getDeclaredConstructor().newInstance()
        } catch (e: NoSuchMethodException) {
            logger.error(e)
        } catch (e: InstantiationException) {
            logger.error(e)
        } catch (e: IllegalAccessException) {
            logger.error(e)
        } catch (e: InvocationTargetException) {
            logger.error(e)
        }
    }

    /**
     * Serializes this gc message instance to a byte array.
     * @return The data representing a client message.
     */
    override fun serialize(): ByteArray {
        try {
            MemoryStream().use { ms ->
                val os = ms.asOutputStream()
                header.serialize(os)
                body.serialize(os)
                os.write(payload.toByteArray())
                return ms.toByteArray()
            }
        } catch (e: IOException) {
            logger.error(e)
        }

        return ByteArray(0)
    }

    /**
     * Initializes this gc message by deserializing the specified data.
     * @param data The data representing a client message.
     */
    override fun deserialize(data: ByteArray) {
        MemoryStream(data).use { ms ->
            try {
                header.deserialize(ms)
                body.deserialize(ms)
            } catch (e: IOException) {
                logger.error(e)
            }

            payload.write(data, ms.position.toInt(), ms.available())
            payload.seek(0, SeekOrigin.BEGIN)
        }
    }
}
