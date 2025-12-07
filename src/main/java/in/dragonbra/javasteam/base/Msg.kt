package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.MsgHdr
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import java.io.IOException
import java.lang.reflect.InvocationTargetException

/**
 * Represents a struct backed message without session or client info.
 *  @constructor Initializes a new instance of the [Msg] class.
 *  @param bodyType       body type
 *  @param payloadReserve The number of bytes to initialize the payload capacity to.
 */
@Suppress("unused")
class Msg<BodyType : ISteamSerializableMessage> @JvmOverloads constructor(
    bodyType: Class<out BodyType>,
    payloadReserve: Int = 0,
) : MsgBase<MsgHdr>(MsgHdr::class.java, payloadReserve) {

    companion object {
        private val logger: Logger = LogManager.getLogger(Msg::class.java)
    }

    /**
     * Gets the structure body of the message.
     */
    lateinit var body: BodyType
        private set

    /**
     * Gets a value indicating whether this client message is protobuf backed.
     * @return **true** if this instance is protobuf backed; otherwise, **false**.
     */
    override val isProto: Boolean
        get() = false

    /**
     * Gets the network message type of this client message.
     */
    override val msgType: EMsg
        get() = header.msg

    /**
     * Gets or sets the session id for this client message.
     * This type of client message does not support session ids
     */
    override var sessionID: Int
        get() = 0
        set(value) { /* No-Op */ }

    /**
     * Gets or sets the <see cref="SteamID"/> for this client message.
     * This type of client message goes not support [SteamID]s.
     */
    override var steamID: SteamID?
        get() = null
        set(value) { /* No-Op */ }

    /**
     * Gets or sets the target job id for this client message.
     */
    override var targetJobID: JobID
        get() = JobID(header.targetJobID)
        set(value) {
            header.targetJobID = value.value
        }

    /**
     * Gets or sets the source job id for this client message.
     */
    override var sourceJobID: JobID
        get() = JobID(header.sourceJobID)
        set(value) {
            header.sourceJobID = value.value
        }

    /**
     * Initializes a new instance of the [Msg] class.
     * This a reply constructor.
     * @param bodyType The body type.
     * @param msg The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    @JvmOverloads
    constructor(
        bodyType: Class<out BodyType>,
        msg: MsgBase<MsgHdr>,
        payloadReserve: Int = 0,
    ) : this(bodyType, payloadReserve) {
        // our target is where the message came from
        header.targetJobID = msg.header.sourceJobID
    }

    /**
     * Initializes a new instance of the [Msg] class.
     * This a receive constructor.
     * @param bodyType The body type
     * @param msg The packet message to build this client message from.
     */
    constructor(bodyType: Class<out BodyType>, msg: IPacketMsg) : this(bodyType) {
        deserialize(msg.data)
    }

    init {
        try {
            body = bodyType.getDeclaredConstructor().newInstance()
        } catch (e: NoSuchMethodException) {
            logger.debug(e)
        } catch (e: InstantiationException) {
            logger.debug(e)
        } catch (e: IllegalAccessException) {
            logger.debug(e)
        } catch (e: InvocationTargetException) {
            logger.debug(e)
        }

        header.setEMsg(body.eMsg)
    }

    /**
     * serializes this client message instance to a byte array.
     * @return Data representing a client message.
     */
    override fun serialize(): ByteArray {
        try {
            MemoryStream(0).use { ms ->
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
     * Initializes this client message by deserializing the specified data.
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
