package in.dragonbra.javasteam.steam.handlers.steamunifiedmessages;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessageV3;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.base.PacketClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodNotification;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.compat.Consumer;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lossy
 * @since 2023-01-04
 * <p>
 * This handler is used for interacting with Steamworks unified messaging
 */
public class SteamUnifiedMessages extends ClientMsgHandler {

    private static final Logger logger = LogManager.getLogger(SteamUnifiedMessages.class);
    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamUnifiedMessages() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ServiceMethodResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleServiceMethodResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ServiceMethod, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleServiceMethod(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    /**
     * Sends a message.
     * Results are returned in a {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     *
     * @param rpcName    Name of the RPC endpoint. Takes the format ServiceName.RpcName
     * @param message    The message to send.
     * @param <TRequest> The type of protobuf object.
     * @return The JobID of the request. This can be used to find the appropriate {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessage(String rpcName, GeneratedMessageV3 message) {
        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }

        JobID jobID = client.getNextJobID();
        EMsg eMsg = client.getSteamID() == null ? EMsg.ServiceMethodCallFromClientNonAuthed : EMsg.ServiceMethodCallFromClient;

        ClientMsgProtobuf<TRequest> msg = new ClientMsgProtobuf<>(message.getClass(), eMsg);
        msg.setSourceJobID(jobID);
        msg.getHeader().getProto().setTargetJobName(rpcName);
        msg.getBody().mergeFrom(message);

        client.send(msg);

        return jobID;
    }

    /**
     * Sends a notification.
     *
     * @param rpcName    Name of the RPC endpoint. Takes the format ServiceName.RpcName
     * @param message    The message to send.
     * @param <TRequest> The type of protobuf object.
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> void sendNotification(String rpcName, GeneratedMessageV3 message) {
        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }

        EMsg eMsg = client.getSteamID() == null ? EMsg.ServiceMethodCallFromClientNonAuthed : EMsg.ServiceMethodCallFromClient;
        ClientMsgProtobuf<TRequest> msg = new ClientMsgProtobuf<>(message.getClass(), eMsg);
        msg.getHeader().getProto().setTargetJobName(rpcName);
        msg.getBody().mergeFrom(message);

        client.send(msg);
    }

    private void handleServiceMethodResponse(IPacketMsg packetMsg) {
        if (!(packetMsg instanceof PacketClientMsgProtobuf)) {
            throw new IllegalArgumentException("Packet message is expected to be protobuf.");
        }

        PacketClientMsgProtobuf packetMsgProto = (PacketClientMsgProtobuf) packetMsg;

        client.postCallback(new ServiceMethodResponse(packetMsgProto));
    }

    @SuppressWarnings("unchecked")
    private void handleServiceMethod(IPacketMsg packetMsg) {
        if (!(packetMsg instanceof PacketClientMsgProtobuf)) {
            throw new IllegalArgumentException("Packet message is expected to be protobuf.");
        }

        PacketClientMsgProtobuf packetMsgProto = (PacketClientMsgProtobuf) packetMsg;

        String jobName = packetMsgProto.getHeader().getProto().getTargetJobName();
        if (!Strings.isNullOrEmpty(jobName)) {
            String[] splitByDot = jobName.split("\\.");
            String[] splitByHash = splitByDot[1].split("#");

            String serviceName = splitByDot[0];
            String methodName = splitByHash[0];

            String serviceInterfaceName = "in.dragonbra.javasteam.rpc.I" + serviceName;
            try {
                logger.debug("Handling Service Method: " + serviceInterfaceName);

                Class<?> serviceInterfaceType = Class.forName(serviceInterfaceName);

                Method method = null;
                for (Method m : serviceInterfaceType.getDeclaredMethods()) {
                    if (m.getName().equals(methodName)) {
                        method = m;
                    }
                }

                if (method != null) {
                    Class<? extends AbstractMessage> argumentType = (Class<? extends AbstractMessage>) method.getParameterTypes()[0];

                    client.postCallback(new ServiceMethodNotification(argumentType, packetMsg));
                }
            } catch (ClassNotFoundException e) {
                // The RPC service implementation was not implemented.
                // Either the .proto is missing, or the service was not converted to an interface yet.
                logger.error("Service Method: " + serviceName + ", was not found");
            }
        }
    }
}
