package in.dragonbra.javasteam.util.log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author lngtr
 * @since 2018-03-02
 */
public class LogManager {

    static final List<LogListener> LOG_LISTENERS = new LinkedList<>();

    private static final Map<Class<?>, Logger> LOGGERS = new HashMap<>();

    /**
     * Gets the {@link Logger} instance of the specified class.
     *
     * @param clazz the class, must not be null.
     * @return the logger instance.
     */
    public static Logger getLogger(Class<?> clazz) {
        return LOGGERS.computeIfAbsent(clazz, k -> new Logger(clazz));
    }

    /**
     * Adds a log listener that will be notified of logging events.
     * You can use the {@link DefaultLogListener} that prints logs to the standard output in a format similar to Log4j2
     *
     * @param listener the listener.
     */
    public static void addListener(LogListener listener) {
        if (listener != null) {
            LOG_LISTENERS.add(listener);
        }
    }

    /**
     * Remove a log listener.
     *
     * @param listener the listener.
     */
    public static void removeListener(LogListener listener) {
        LOG_LISTENERS.remove(listener);
    }

    private LogManager() {
    }
}
