package in.dragonbra.javasteam.util.log;

/**
 * @author lngtr
 * @since 2018-03-02
 */
public class Logger {

    private final Class<?> clazz;

    Logger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        this.clazz = clazz;
    }

    public void debug(Throwable throwable) {
        debug(null, throwable);
    }

    public void debug(String message) {
        debug(message, null);
    }

    public void debug(String message, Throwable throwable) {
        for (LogListener listener : LogManager.LOG_LISTENERS) {
            if (listener != null) {
                listener.onLog(clazz, message, throwable);
            }
        }
    }

    public void error(Throwable throwable) {
        error(null, throwable);
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable throwable) {
        for (LogListener listener : LogManager.LOG_LISTENERS) {
            if (listener != null) {
                listener.onError(clazz, message, throwable);
            }
        }
    }
}
