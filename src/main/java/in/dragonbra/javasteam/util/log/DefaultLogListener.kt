package `in`.dragonbra.javasteam.util.log

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author lngtr
 * @since 2018-03-02
 */
class DefaultLogListener : LogListener {

    companion object {
        private val FORMAT = SimpleDateFormat("HH:mm:ss.SSS")
    }

    override fun onLog(clazz: Class<*>, message: String?, throwable: Throwable?) {
        val threadName = Thread.currentThread().name.take(10)

        if (message == null) {
            System.out.printf("%s [%10s] %s%n", FORMAT.format(Date()), threadName, clazz.name)
        } else {
            System.out.printf("%s [%10s] %s - %s%n", FORMAT.format(Date()), threadName, clazz.name, message)
        }

        throwable?.printStackTrace()
    }

    override fun onError(clazz: Class<*>, message: String?, throwable: Throwable?) {
        val threadName = Thread.currentThread().name.take(10)

        if (message == null) {
            System.err.printf("%s [%10s] %s%n", FORMAT.format(Date()), threadName, clazz.name)
        } else {
            System.err.printf("%s [%10s] %s - %s%n", FORMAT.format(Date()), threadName, clazz.name, message)
        }

        throwable?.printStackTrace()
    }
}
