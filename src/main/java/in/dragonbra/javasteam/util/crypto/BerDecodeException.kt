package `in`.dragonbra.javasteam.util.crypto

class BerDecodeException @JvmOverloads constructor(
    msg: String? = null,
    val position: Int = 0,
    cause: Exception? = null,
) : Exception(msg, cause) {
    override val message: String
        get() = super.message + " (Position $position)${System.lineSeparator()}"
}
