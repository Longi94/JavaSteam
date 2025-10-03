package `in`.dragonbra.javasteam.util.log

import `in`.dragonbra.javasteam.util.JavaSteamAddition
import java.util.*

/**
 * @author lngtr
 * @since 2018-03-02
 */
object LogManager {

    private val LOGGERS: MutableMap<Class<*>, Logger> = HashMap<Class<*>, Logger>()

    @JvmField
    val LOG_LISTENERS: MutableList<LogListener> = LinkedList<LogListener>()

    /**
     * Gets the [Logger] instance of the specified class.
     * @param clazz the class, must not be null.
     * @return the logger instance.
     */
    @JvmStatic
    fun getLogger(clazz: Class<*>): Logger = LOGGERS.computeIfAbsent(clazz) { Logger(clazz) }

    /**
     * Adds a log listener that will be notified of logging events.
     * You can use the [DefaultLogListener] that prints logs to the standard output in a format similar to Log4j2
     * @param listener the listener.
     */
    @JvmStatic
    fun addListener(listener: LogListener) {
        LOG_LISTENERS.add(listener)
    }

    /**
     * Remove a log listener.
     * @param listener the listener.
     */
    @JvmStatic
    fun removeListener(listener: LogListener) {
        LOG_LISTENERS.remove(listener)
    }

    // Kotlin Helpers

    /**
     * Gets the [Logger] instance of the specified class.
     * @param T the class, must not be null.
     * @return the logger instance.
     */
    @JavaSteamAddition
    inline fun <reified T> getLogger(): Logger = getLogger(T::class.java)
}
