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

    // private static final Logger logger = LogManager.getLogger(UnifiedService.class);

    private final SteamUnifiedMessages steamUnifiedMessages;

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @param parentClassName The parent class name, ie: Player
     * @param methodName      The calling method name, ie: GetGameBadgeLevels
     * @return The name of the RPC endpoint as formatted ServiceName.RpcName. ie: Player.GetGameBadgeLevels#1
     */
    private static String getRpcName(String parentClassName, String methodName) {
        return String.format("%s.%s#%s", parentClassName, methodName, 1);
    }

    /**
     * Gets the calling method name. ie: GetGameBadgeLevels()...
     *
     * @return The calling method name.
     */
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[3].getMethodName();
    }

    public UnifiedService(SteamUnifiedMessages steamUnifiedMessages) {
        this.steamUnifiedMessages = steamUnifiedMessages;
    }

    /**
     * Sends a message.
     * <p>
     * Results are returned in a {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     *
     * @param clazz      The type of the body, for type erasure
     * @param message    The message to send.
     * @param <TRequest> The type of protobuf object.
     * @return The JobID of the message. This can be used to find the appropriate {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessage(Class<? extends AbstractMessage> clazz, TRequest message) {
        String serviceName = getClassName();
        String rpcName = getMethodName();

        return sendMessageOrNotification(clazz, getRpcName(serviceName, rpcName), message, false);
    }

    /**
     * Sends a notification.
     *
     * @param clazz      The type of the body, for type erasure
     * @param message    The message to send.
     * @param <TRequest> The type of protobuf object.
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> void sendNotification(Class<? extends AbstractMessage> clazz, TRequest message) {
        String serviceName = getClassName();
        String rpcName = getMethodName();


        sendMessageOrNotification(clazz, getRpcName(serviceName, rpcName), message, true);
    }

    private <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessageOrNotification
            (Class<? extends AbstractMessage> clazz, String rpcName, TRequest message, Boolean isNotification) {

        if (isNotification) {
            steamUnifiedMessages.sendNotification(clazz, rpcName, message);
            return null;
        }

        return steamUnifiedMessages.sendMessage(clazz, rpcName, message);
    }
}
