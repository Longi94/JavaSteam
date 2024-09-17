package `in`.dragonbra.javasteam.util.compat

/**
 * Compat class basically kotlin stdlib Function.kt
 * https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/runtime/kotlin/jvm/functions/FunctionCompat1.kt
 */
interface FunctionCompat1<in P1, out R> : Function<R> {
    /** Invokes the function with the specified argument. */
    operator fun invoke(p1: P1): R
}
