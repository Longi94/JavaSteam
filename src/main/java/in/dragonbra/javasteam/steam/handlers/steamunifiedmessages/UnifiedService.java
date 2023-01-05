package in.dragonbra.javasteam.steam.handlers.steamunifiedmessages;

import com.google.protobuf.AbstractMessage;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public abstract class UnifiedService {

    // private static final Logger logger = LogManager.getLogger(UnifiedService.class);

    private final SteamUnifiedMessages steamUnifiedMessages;

    public UnifiedService(SteamUnifiedMessages steamUnifiedMessages) {
        this.steamUnifiedMessages = steamUnifiedMessages;
    }

    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @param parentClassName The parent class name, ie: Player
     * @param methodName      The calling method name, ie: GetGameBadgeLevels
     * @return The name of the RPC endpoint as formatted ServiceName.RpcName. ie: Player.GetGameBadgeLevels#1
     */
    private static String getRpcEndpoint(String parentClassName, String methodName) {
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

    /**
     * Sends a message.
     * <p>
     * Results are returned in a {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     *
     * @param message The message to send.
     * @return The JobID of the message. This can be used to find the appropriate {@link in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse}.
     */
    public JobID sendMessage(AbstractMessage message) {
        String serviceName = getClassName();
        String rpcName = getMethodName();
        String rpcEndpoint = getRpcEndpoint(serviceName, rpcName);

        return sendMessageOrNotification(message.getClass(), rpcEndpoint, message, false);
    }

    /**
     * Sends a notification.
     *
     * @param message The message to send.
     */
    public void sendNotification(AbstractMessage message) {
        String serviceName = getClassName();
        String rpcName = getMethodName();
        String rpcEndpoint = getRpcEndpoint(serviceName, rpcName);

        sendMessageOrNotification(message.getClass(), rpcEndpoint, message, true);
    }

    private JobID sendMessageOrNotification
            (Class<? extends AbstractMessage> clazz, String rpcName, AbstractMessage message, Boolean isNotification) {

        if (isNotification) {
            steamUnifiedMessages.sendNotification(clazz, rpcName, message);
            return null;
        }

        return steamUnifiedMessages.sendMessage(clazz, rpcName, message);
    }
}
