package in.dragonbra.javasteam.util.log;

/**
 * @author lngtr
 * @since 2018-03-02
 */
public interface LogListener {
    void onLog(Class<?> clazz, String message, Throwable throwable);

    void onError(Class<?> clazz, String message, Throwable throwable);
}
