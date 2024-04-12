package in.dragonbra.javasteam.base;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Represents a protobuf backed client message.
 *
 * @param <BodyType> The body type of this message.
 */
public class ClientMsgProtobuf<BodyType extends GeneratedMessage.Builder<BodyType>> extends AClientMsgProtobuf {

    private static final Logger logger = LogManager.getLogger(ClientMsgProtobuf.class);

    private BodyType body;

    private final Class<? extends AbstractMessage> clazz;

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a client send constructor.
     *
     * @param clazz the type of the body
     * @param msg   The network message type this client message represents.
     */
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, IPacketMsg msg) {
        this(clazz, msg, 64);
        if (!msg.isProto()) {
            logger.debug("ClientMsgProtobuf<" + clazz.getSimpleName() + "> used for non-proto message!");
        }
        deserialize(msg.getData());
    }

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a client send constructor.
     *
     * @param clazz          the type of the body
     * @param msg            The network message type this client message represents.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, IPacketMsg msg, int payloadReserve) {
        this(clazz, msg.getMsgType(), payloadReserve);
    }

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a client send constructor.
     *
     * @param clazz the type of the body
     * @param eMsg  The network message type this client message represents.
     */
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, EMsg eMsg) {
        this(clazz, eMsg, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a client send constructor.
     *
     * @param clazz          the type of the body
     * @param eMsg           The network message type this client message represents.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    @SuppressWarnings("unchecked")
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, EMsg eMsg, int payloadReserve) {
        super(payloadReserve);
        this.clazz = clazz;

        try {
            final Method m = clazz.getMethod("newBuilder");
            body = (BodyType) m.invoke(null);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.debug(e);
        }

        getHeader().setEMsg(eMsg);
    }

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a reply constructor.
     *
     * @param clazz the type of the body
     * @param eMsg  The network message type this client message represents.
     * @param msg   The message that this instance is a reply for.
     */
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, EMsg eMsg, MsgBase<MsgHdrProtoBuf> msg) {
        this(clazz, eMsg, msg, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientMsgProtobuf} class.
     * This is a reply constructor.
     *
     * @param clazz          the type of the body
     * @param eMsg           The network message type this client message represents.
     * @param msg            The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientMsgProtobuf(Class<? extends AbstractMessage> clazz, EMsg eMsg, MsgBase<MsgHdrProtoBuf> msg, int payloadReserve) {
        this(clazz, eMsg, payloadReserve);
        // our target is where the message came from
        getHeader().getProto().setJobidTarget(msg.getHeader().getProto().getJobidSource());
    }

    /**
     * @return the body structure of this message.
     */
    public BodyType getBody() {
        return body;
    }

    /**
     * Sets the body of this message.
     *
     * @param _body the body structure of this message.
     */
    public void setBody(BodyType _body) {
        this.body = _body;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(0);

        try {
            getHeader().serialize(baos);
            baos.write(body.build().toByteArray());
            baos.write(payload.toByteArray());
        } catch (IOException e) {
            logger.debug(e);
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        BinaryReader ms = new BinaryReader(new ByteArrayInputStream(data));

        try {
            getHeader().deserialize(ms);
            final Method m = clazz.getMethod("newBuilder");
            body = (BodyType) m.invoke(null);
            body.mergeFrom(ms);
            payload.write(data, ms.getPosition(), ms.available());
            payload.seek(0, SeekOrigin.BEGIN);
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            logger.debug(e);
        }

    }
}
