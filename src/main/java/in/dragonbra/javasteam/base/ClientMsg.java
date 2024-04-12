package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.ExtendedClientMsgHdr;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents a struct backed client message.
 *
 * @param <BodyType> The body type of this message.
 */
@SuppressWarnings("unused")
public class ClientMsg<BodyType extends ISteamSerializableMessage> extends MsgBase<ExtendedClientMsgHdr> {

    private static final Logger logger = LogManager.getLogger(ClientMsg.class);

    private BodyType body;

    /**
     * Initializes a new instance of the {@link ClientMsg} class.
     *
     * @param bodyType body type
     */
    public ClientMsg(Class<? extends BodyType> bodyType) {
        this(bodyType, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientMsg} class.
     *
     * @param bodyType       body type
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientMsg(Class<? extends BodyType> bodyType, int payloadReserve) {
        super(ExtendedClientMsgHdr.class, payloadReserve);

        try {
            body = bodyType.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException e) {
            logger.debug(e);
        }

        getHeader().setEMsg(body.getEMsg());
    }

    /**
     * Initializes a new instance of the {@link ClientMsg} class.
     * This a reply constructor.
     *
     * @param bodyType body type
     * @param msg      The message that this instance is a reply for.
     */
    public ClientMsg(Class<? extends BodyType> bodyType, MsgBase<ExtendedClientMsgHdr> msg) {
        this(bodyType, msg, 64);
    }

    /**
     * Initializes a new instance of the {@link ClientMsg} class.
     * This a reply constructor.
     *
     * @param bodyType       body type
     * @param msg            The message that this instance is a reply for.
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public ClientMsg(Class<? extends BodyType> bodyType, MsgBase<ExtendedClientMsgHdr> msg, int payloadReserve) {
        this(bodyType, payloadReserve);

        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        // our target is where the message came from
        getHeader().setTargetJobID(msg.getHeader().getSourceJobID());
    }

    /**
     * Initializes a new instance of the {@link ClientMsg} class.
     * This a receive constructor.
     *
     * @param bodyType body type
     * @param msg      The packet message to build this client message from.
     */
    public ClientMsg(Class<? extends BodyType> bodyType, IPacketMsg msg) {
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
    public EMsg getMsgType() {
        return getHeader().getMsg();
    }

    @Override
    public int getSessionID() {
        return getHeader().getSessionID();
    }

    @Override
    public void setSessionID(int sessionID) {
        getHeader().setSessionID(sessionID);
    }

    @Override
    public SteamID getSteamID() {
        return getHeader().getSteamID();
    }

    @Override
    public void setSteamID(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }
        getHeader().setSteamID(steamID);
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

    /**
     * @return the body structure of this message.
     */
    public BodyType getBody() {
        return body;
    }
}
