package `in`.dragonbra.javasteam.networking.steam3

import java.util.EnumSet

/**
 * The type of communications protocol to use when communicating with the Steam backend
 *
 * @author lngtr
 * @since 2018-02-20
 */
enum class ProtocolTypes(private val code: Int) {
    // TCP
    TCP(1),

    // UDP
    UDP(1 shl 1),

    // WebSockets (HTTP / TLS)
    WEB_SOCKET(1 shl 2),
    ;

    fun code(): Int = code

    companion object {
        /**
         * All available protocol types
         */
        @JvmField
        val ALL: EnumSet<ProtocolTypes> = EnumSet.allOf(ProtocolTypes::class.java)

        @JvmStatic
        fun from(code: Int): EnumSet<ProtocolTypes> {
            val set = EnumSet.noneOf(ProtocolTypes::class.java)
            for (e in entries) {
                if ((e.code and code) != 0) {
                    set.add(e)
                }
            }
            return set
        }

        @JvmStatic
        fun code(flags: EnumSet<ProtocolTypes>): Int = flags.fold(0) { acc, flag -> acc or flag.code }
    }
}
