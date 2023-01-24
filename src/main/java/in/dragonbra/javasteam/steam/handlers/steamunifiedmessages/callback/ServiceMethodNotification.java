package in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback;

import com.google.protobuf.AbstractMessage;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * @author Lossy
 * @since 2023-01-04
 * <p>
 * This callback represents a service notification received though {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages}.
 */
@SuppressWarnings("unused")
public class ServiceMethodNotification extends CallbackMsg {

    private final String methodName;

    private final Object body;

    public ServiceMethodNotification(Class<? extends AbstractMessage> messageType, IPacketMsg packetMsg) {
        // Bounce into generic-land.
        ClientMsgProtobuf<?> clientMsg = new ClientMsgProtobuf<>(messageType, packetMsg);

        // Note: JobID will be -1

        this.methodName = clientMsg.getHeader().getProto().getTargetJobName();
        this.body = clientMsg.getBody().build();
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
     * @return Gets the protobuf notification body.
     */
    public Object getBody() {
        return body;
    }
}
