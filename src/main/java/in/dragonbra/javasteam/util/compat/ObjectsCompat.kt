package `in`.dragonbra.javasteam.util.compat

/**
 * Compatibility for [java.util.Objects] for Android, which requires API 19+.
 * @author steev
 * @since 2018-03-21
 */
object ObjectsCompat {
    @JvmStatic
    fun equals(a: Any?, b: Any?): Boolean = a == b
}
