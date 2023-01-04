package in.dragonbra.javasteam.steam.handlers.steamunifiedmessages;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessageV3;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public abstract class UnifiedService {

    private final SteamUnifiedMessages steamUnifiedMessages;

    /**
     * Cast the protobuf request class into a GeneratedMessageV3.Builder class to make ClientMsgProtobuf happy.
     *
     * @param object     The request .builder()
     * @param clazz      Type erasure to the Builder class.
     * @param <TRequest> The type parameter for the object to be cast to.
     * @return the class that's been cast.
     */
    public static <TRequest extends GeneratedMessageV3.Builder<TRequest>> TRequest convertInstanceOfObject(Object object, Class<TRequest> clazz) {
        try {
            return clazz.cast(object);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param parentClassName The parent class name, ie: Player
     * @param methodName      The calling method name
     * @return The formatted string with current service version: Player.GetGameBadgeLevels#1
     */
    private String getTargetJobName(String parentClassName, String methodName) {
        return String.format("%s.%s#%s", parentClassName, methodName, 1);
    }

    /**
     * Gets the calling method name. ie: GetGameBadgeLevels()...
     *
     * @return The calling method name.
     */
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[2].getMethodName(); // Guaranteed 100% terrible!
    }

    /**
     * Public constructor.
     *
     * @param steamUnifiedMessages The instance of SteamUnifiedMessages
     */
    public UnifiedService(SteamUnifiedMessages steamUnifiedMessages) {
        this.steamUnifiedMessages = steamUnifiedMessages;
    }

    /**
     * Sends a message.
     * <p>
     * Results are returned in a {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     *
     * @param clazz       The type of protobuf object request.
     * @param serviceName The service name, ServiceName.RpcName.
     * @param rpcName     The rpc name of the service, ServiceName.RpcName.
     * @param request     The requested protobuf object's builder
     * @param <TRequest>  The request type parameter.
     * @return The JobID of the request. This can be used to find the appropriate {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessage
    (Class<? extends AbstractMessage> clazz, String serviceName, String rpcName, TRequest request) {

        return sendMessageOrNotification(clazz, getTargetJobName(serviceName, rpcName), request, false);
    }

    /**
     * Sends a notification.
     *
     * @param clazz       The type of protobuf object request.
     * @param serviceName The service name, ServiceName.RpcName.
     * @param rpcName     The rpc name of the service, ServiceName.RpcName.
     * @param request     The type of the protobuf object which is the response to the RPC call.
     * @param <TRequest>  The request type parameter
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> void sendNotification
    (Class<? extends AbstractMessage> clazz, String serviceName, String rpcName, TRequest request) {

        sendMessageOrNotification(clazz, getTargetJobName(serviceName, rpcName), request, true);
    }

    private <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessageOrNotification
            (Class<? extends AbstractMessage> clazz, String name, TRequest message, Boolean isNotification) {

        if (isNotification) {
            steamUnifiedMessages.sendNotification(clazz, name, message);
            return null;
        }

        return steamUnifiedMessages.sendMessage(clazz, name, message);
    }
}
