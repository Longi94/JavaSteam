package `in`.dragonbra.javasteam.util.log

/**
 * @author lngtr
 * @since 2018-03-02
 */
interface LogListener {
    fun onLog(clazz: Class<*>, message: String?, throwable: Throwable?)
    fun onError(clazz: Class<*>, message: String?, throwable: Throwable?)
}
