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
     * @param o          the request .builder()
     * @param clazz      type erasure to the Builder class.
     * @param <TRequest> GeneratedMessageV3.Builder<TRequest>
     * @return the class that's been cast.
     */
    public static <TRequest extends GeneratedMessageV3.Builder<TRequest>> TRequest convertInstanceOfObject(Object o, Class<TRequest> clazz) {
        try {
            return clazz.cast(o);
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
     * @param clazz
     * @param parentClassName
     * @param methodName
     * @param request
     * @param <TRequest>
     * @return
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessage
    (Class<? extends AbstractMessage> clazz, String parentClassName, String methodName, TRequest request) {

        return sendMessageOrNotification(clazz, getTargetJobName(parentClassName, methodName), request, false);
    }

    /**
     * @param clazz
     * @param parentClassName
     * @param methodName
     * @param <TRequest>
     * @param request
     */
    public <TRequest extends GeneratedMessageV3.Builder<TRequest>> void sendNotification
    (Class<? extends AbstractMessage> clazz, String parentClassName, String methodName, TRequest request) {

        sendMessageOrNotification(clazz, getTargetJobName(parentClassName, methodName), request, true);
    }

    /**
     * @param clazz
     * @param name
     * @param message
     * @param isNotification
     * @param <TRequest>
     * @return
     */
    private <TRequest extends GeneratedMessageV3.Builder<TRequest>> JobID sendMessageOrNotification
    (Class<? extends AbstractMessage> clazz, String name, TRequest message, Boolean isNotification) {

        if (isNotification) {
            steamUnifiedMessages.sendNotification(clazz, name, message);
            return null;
        }

        return steamUnifiedMessages.sendMessage(clazz, name, message);
    }
}
