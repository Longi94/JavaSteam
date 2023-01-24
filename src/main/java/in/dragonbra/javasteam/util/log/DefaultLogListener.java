package in.dragonbra.javasteam.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lngtr
 * @since 2018-03-02
 */
public class DefaultLogListener implements LogListener {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    @Override
    public void onLog(Class<?> clazz, String message, Throwable throwable) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        String threadName = Thread.currentThread().getName();
        threadName = threadName.substring(0, Math.min(10, threadName.length()));
        String className = clazz.getName();

        if (message == null) {
            System.out.printf("%s [%10s] %s%n", FORMAT.format(new Date()), threadName, className);
        } else {
            System.out.printf("%s [%10s] %s - %s%n", FORMAT.format(new Date()), threadName, className, message);
        }

        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    public void onError(Class<?> clazz, String message, Throwable throwable) {
        if (clazz == null) {
            throw new IllegalArgumentException("class is null");
        }
        String threadName = Thread.currentThread().getName();
        threadName = threadName.substring(0, Math.min(10, threadName.length()));
        String className = clazz.getName();

        if (message == null) {
            System.err.printf("%s [%10s] %s%n", FORMAT.format(new Date()), threadName, className);
        } else {
            System.err.printf("%s [%10s] %s - %s%n", FORMAT.format(new Date()), threadName, className, message);
        }

        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
