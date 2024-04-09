package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.generated.MsgGCHdr;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents a struct backed game coordinator message.
 *
 * @param <BodyType> The body type of this message.
 */
@SuppressWarnings("unused")
public class ClientGCMsg<BodyType extends IGCSerializableMessage> extends GCMsgBase<MsgGCHdr> {

    private static final Logger logger = LogManager.getLogger(ClientGCMsg.class);

    private final int msgType;

    private BodyType body;

    /**
     * Initializes a new instance of the {@link ClientGCMsg} class.
     *
     * @param bodyType body type
     */
    public ClientGCMsg(Class<? extends BodyType> bodyType) {
        this(bodyType, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientGCMsg} class.
     *
     * @param bodyType       body type
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientGCMsg(Class<? extends BodyType> bodyType, int payloadReserve) {
        super(MsgGCHdr.class, payloadReserve);

        try {
            body = bodyType.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException e) {
            logger.debug(e);
        }

        msgType = body.getEMsg();
    }

    /**
     * Initializes a new instance of the {@link ClientGCMsg} class.
     * This a reply constructor.
     *
     * @param bodyType body type
     * @param msg      The message that this instance is a reply for.
     */
    public ClientGCMsg(Class<? extends BodyType> bodyType, GCMsgBase<MsgGCHdr> msg) {
        this(bodyType, msg, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientGCMsg} class.
     * This a reply constructor.
     *
     * @param bodyType       body type
     * @param msg            The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientGCMsg(Class<? extends BodyType> bodyType, GCMsgBase<MsgGCHdr> msg, int payloadReserve) {
        this(bodyType, payloadReserve);

        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        // our target is where the message came from
        getHeader().setTargetJobID(msg.getHeader().getSourceJobID());
    }

    /**
     * Initializes a new instance of the {@link ClientGCMsg} class.
     * This a receive constructor.
     *
     * @param bodyType body type
     * @param msg      The packet message to build this client message from.
     */
    public ClientGCMsg(Class<? extends BodyType> bodyType, IPacketGCMsg msg) {
        this(bodyType);

        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        if (msg.isProto()) {
            logger.debug("ClientMsg<" + bodyType.getName() + "> used for proto message!");
        }

        deserialize(msg.getData());
    }

    @Override
    public boolean isProto() {
        return false;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

    @Override
    public JobID getTargetJobID() {
        return new JobID(getHeader().getTargetJobID());
    }

    @Override
    public void setTargetJobID(JobID jobID) {
        if (jobID == null) {
            throw new IllegalArgumentException("jobID is null");
        }
        getHeader().setTargetJobID(jobID.getValue());
    }

    @Override
    public JobID getSourceJobID() {
        return new JobID(getHeader().getSourceJobID());
    }

    @Override
    public void setSourceJobID(JobID jobID) {
        if (jobID == null) {
            throw new IllegalArgumentException("jobID is null");
        }
        getHeader().setSourceJobID(jobID.getValue());
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(0);

        try {
            getHeader().serialize(baos);
            body.serialize(baos);
            baos.write(payload.toByteArray());
        } catch (IOException e) {
            logger.debug(e);
        }

        return baos.toByteArray();
    }

    @Override
    public void deserialize(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        MemoryStream ms = new MemoryStream(data);

        try {
            getHeader().deserialize(ms);
            body.deserialize(ms);
        } catch (IOException e) {
            logger.debug(e);
        }

        payload.write(data, (int) ms.getPosition(), ms.available());
        payload.seek(0, SeekOrigin.BEGIN);
    }
}
