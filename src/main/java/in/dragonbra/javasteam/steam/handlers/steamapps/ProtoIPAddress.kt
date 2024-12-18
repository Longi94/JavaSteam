package `in`.dragonbra.javasteam.steam.handlers.steamapps

abstract class ProtoIPAddress<T> {
    abstract fun getValue(): T
}
data class ProtoIPv4(
    private val ip: Int
) : ProtoIPAddress<Int>() {
    override fun getValue(): Int {
        return ip
    }
}
@Suppress("ArrayInDataClass")
data class ProtoIPv6(
    private val ip: ByteArray
) : ProtoIPAddress<ByteArray>() {
    override fun getValue(): ByteArray {
        return ip
    }
}
