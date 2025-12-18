package `in`.dragonbra.javasteam.base

import com.google.protobuf.AbstractMessage
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.MsgHdrProtoBuf
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException

/**
 * Represents a protobuf backed client message.
 * @constructor Initializes a new instance of the [ClientMsgProtobuf] class.
 * This is a client send constructor.
 * @param clazz The type of the body
 * @param eMsg The network message type this client message represents.
 * @param payloadReserve The number of bytes to initialize the payload capacity to.
 */
class ClientMsgProtobuf<BodyType : GeneratedMessage.Builder<BodyType>> @JvmOverloads constructor(
    private val clazz: Class<out AbstractMessage>,
    eMsg: EMsg,
    payloadReserve: Int = 64,
) : AClientMsgProtobuf(payloadReserve) {

    companion object {
        private val logger: Logger = LogManager.getLogger(ClientMsgProtobuf::class.java)
    }

    /**
     * Gets the body structure of this message.
     */
    lateinit var body: BodyType
        private set

    /**
     * Initializes a new instance of the [ClientMsgProtobuf] class.
     * This is a client send constructor.
     *
     * @param clazz the type of the body
     * @param msg   The network message type this client message represents.
     */
    constructor(clazz: Class<out AbstractMessage>, msg: IPacketMsg) : this(clazz, msg, 64) {
        if (!msg.isProto) {
            logger.debug("ClientMsgProtobuf<" + clazz.getSimpleName() + "> used for non-proto message!")
        }
        deserialize(msg.data)
    }

    /**
     * Initializes a new instance of the [ClientMsgProtobuf] class.
     * This is a client send constructor.
     *
     * @param clazz          the type of the body
     * @param msg            The network message type this client message represents.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    constructor(clazz: Class<out AbstractMessage>, msg: IPacketMsg, payloadReserve: Int) : this(
        clazz,
        msg.msgType,
        payloadReserve
    )

    /**
     * Initializes a new instance of the [ClientMsgProtobuf] class.
     * This is a reply constructor.
     *
     * @param clazz          the type of the body
     * @param eMsg           The network message type this client message represents.
     * @param msg            The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    @JvmOverloads
    constructor(
        clazz: Class<out AbstractMessage>,
        eMsg: EMsg,
        msg: MsgBase<MsgHdrProtoBuf>,
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
            logger.debug(e)
        } catch (e: NoSuchMethodException) {
            logger.debug(e)
        } catch (e: InvocationTargetException) {
            logger.debug(e)
        }

        header.setEMsg(eMsg)
    }

    /**
     * Serializes this client message instance to a byte array.
     * @return The ata representing a client message.
     */
    override fun serialize(): ByteArray {
        try {
            ByteArrayOutputStream(0).use { baos ->
                header.serialize(baos)
                baos.write(body.build().toByteArray())
                baos.write(payload.toByteArray())
                return baos.toByteArray()
            }
        } catch (e: IOException) {
            logger.debug(e)
        }

        return ByteArray(0)
    }

    /**
     * Initializes this client message by deserializing the specified data.
     * @param data The data representing a client message.
     */
    override fun deserialize(data: ByteArray) {
        val ms = BinaryReader(ByteArrayInputStream(data))

        try {
            header.deserialize(ms)
            val m = clazz.getMethod("newBuilder")
            @Suppress("UNCHECKED_CAST")
            body = m.invoke(null) as BodyType
            body.mergeFrom(ms)
            payload.write(data, ms.position, ms.available())
            payload.seek(0, SeekOrigin.BEGIN)
        } catch (e: IOException) {
            logger.debug(e)
        } catch (e: IllegalAccessException) {
            logger.debug(e)
        } catch (e: NoSuchMethodException) {
            logger.debug(e)
        } catch (e: InvocationTargetException) {
            logger.debug(e)
        }

        // TODO can be MemoryStream
        try {
            ms.close()
        } catch (e: IOException) {
            logger.error(e)
        }
    }
}
