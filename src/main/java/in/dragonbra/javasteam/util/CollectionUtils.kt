package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.util.compat.ObjectsCompat

/**
 * @author lngtr
 * @since 2018-02-19
 */
object CollectionUtils {
    @JvmStatic
    fun <T, E> getKeyByValue(map: Map<T, E>, value: E): T? = map.entries.firstOrNull {
        ObjectsCompat.equals(value, it.value)
    }?.key
}
