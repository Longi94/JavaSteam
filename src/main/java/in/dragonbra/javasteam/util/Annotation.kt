@file:Suppress("ktlint:standard:filename")

package `in`.dragonbra.javasteam.util

/**
 * This annotation indicates that this API is specific to JavaSteam
 * and does not exist in the original SteamKit implementation.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE) // Only in source
@MustBeDocumented
annotation class JavaSteamAddition(val reason: String = "")
