package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Represents a protobuf backed client message. Only contains the header information.
 */
public class AClientMsgProtobuf extends MsgBase<MsgHdrProtoBuf> {

    private static final Logger logger = LogManager.getLogger(AClientMsgProtobuf.class);

    /**
     * Initializes a new instance of the{@link AClientMsgProtobuf} class.
     * This is a recieve constructor.
     *
     * @param msg The packet message to build this client message from.
     */
    public AClientMsgProtobuf(IPacketMsg msg) {
        this(msg.getMsgType());

        if (!msg.isProto()) {
            logger.debug("ClientMsgProtobuf used for non-proto message!");
        }

        deserialize(msg.getData());
    }

    private AClientMsgProtobuf() {
        this(0);
    }

    AClientMsgProtobuf(int payloadReserve) {
        super(MsgHdrProtoBuf.class, payloadReserve);
    }

    private AClientMsgProtobuf(EMsg eMsg) {
        this(eMsg, 0);
    }

    private AClientMsgProtobuf(EMsg eMsg, int payloadReserve) {
        super(MsgHdrProtoBuf.class, payloadReserve);
        // set our emsg
        getHeader().setEMsg(eMsg);
    }

    public CMsgProtoBufHeader.Builder getProtoHeader() {
        return getHeader().getProto();
    }

    @Override
    public boolean isProto() {
        return true;
    }

    @Override
    public EMsg getMsgType() {
        return getHeader().getMsg();
    }

    @Override
    public int getSessionID() {
        return getProtoHeader().getClientSessionid();
    }

    @Override
    public void setSessionID(int sessionID) {
        getProtoHeader().setClientSessionid(sessionID);
    }

    @Override
    public SteamID getSteamID() {
        return new SteamID(getProtoHeader().getSteamid());
    }

    @Override
    public void setSteamID(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }
        getProtoHeader().setSteamid(steamID.convertToUInt64());
    }

    @Override
    public JobID getTargetJobID() {
        return new JobID(getProtoHeader().getJobidTarget());
    }

    @Override
    public void setTargetJobID(JobID jobID) {
        if (jobID == null) {
            throw new IllegalArgumentException("jobID is null");
        }
        getProtoHeader().setJobidTarget(jobID.getValue());
    }

    @Override
    public JobID getSourceJobID() {
        return new JobID(getProtoHeader().getJobidSource());
    }

    @Override
    public void setSourceJobID(JobID jobID) {
        if (jobID == null) {
            throw new IllegalArgumentException("jobID is null");
        }
        getProtoHeader().setJobidSource(jobID.getValue());
    }

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException("ClientMsgProtobuf is for reading only. Use ClientMsgProtobuf<T> for serializing messages.");
    }

    @Override
    public void deserialize(byte[] data) {
        try {
            getHeader().deserialize(new ByteArrayInputStream(data));
        } catch (IOException e) {
            logger.debug(e);
        }
    }
}
