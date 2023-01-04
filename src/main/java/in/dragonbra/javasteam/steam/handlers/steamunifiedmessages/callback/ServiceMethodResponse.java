package in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessageV3;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.PacketClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 * <p>
 * This callback is returned in response to a service method sent through {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages}.
 */
@SuppressWarnings("unused")
public class ServiceMethodResponse extends CallbackMsg {

    private final EResult result;

    private final String methodName;

    private final PacketClientMsgProtobuf packetMsg;

    public ServiceMethodResponse(PacketClientMsgProtobuf packetMsg) {
        CMsgProtoBufHeader protoHeader = packetMsg.getHeader().getProto().build();

        JobID jobID = new JobID(protoHeader.getJobidTarget());
        setJobID(jobID);

        this.result = EResult.from(protoHeader.getEresult());
        this.methodName = protoHeader.getTargetJobName();
        this.packetMsg = packetMsg;
    }

    /**
     * @return Gets the result of the message.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return Gets the name of the Service.
     */
    public String getServiceName() {
        return methodName.split("\\.")[0];
    }

    /**
     * @return Gets the name of the RPC method.
     */
    public String getRpcName() {
        return methodName.substring(getServiceName().length() + 1).split("#")[0];
    }

    /**
     * @return Gets the full name of the service method.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Deserializes the response into a protobuf object.
     *
     * @param clazz The message class, type erasure.
     * @param <T>   Protobuf type of the response message.
     * @return The response to the message sent through {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages}.
     */
    public <T extends GeneratedMessageV3.Builder<T>> T getDeserializedResponse(Class<? extends AbstractMessage> clazz) {
        ClientMsgProtobuf<T> msg = new ClientMsgProtobuf<>(clazz, packetMsg);
        return msg.getBody();
    }
}
