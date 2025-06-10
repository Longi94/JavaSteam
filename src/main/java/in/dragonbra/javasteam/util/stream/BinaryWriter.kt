package `in`.dragonbra.javasteam.util.stream

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
/**
 * Basically DataOutputStream, but the bytes are parsed in reverse order
 */
class BinaryWriter(out: OutputStream) : FilterOutputStream(out) {

    private val writeBuffer = ByteArray(8)

    @Throws(IOException::class)
    fun writeInt(v: Int) {
        out.write(v and 0xFF)
        out.write((v shr 8) and 0xFF)
        out.write((v shr 16) and 0xFF)
        out.write((v shr 24) and 0xFF)
    }

    @Throws(IOException::class)
    fun writeShort(v: Short) {
        out.write(v.toInt() and 0xFF)
        out.write((v.toInt() shr 8) and 0xFF)
    }

    @Throws(IOException::class)
    fun writeLong(v: Long) {
        writeBuffer[7] = (v shr 56).toByte()
        writeBuffer[6] = (v shr 48).toByte()
        writeBuffer[5] = (v shr 40).toByte()
        writeBuffer[4] = (v shr 32).toByte()
        writeBuffer[3] = (v shr 24).toByte()
        writeBuffer[2] = (v shr 16).toByte()
        writeBuffer[1] = (v shr 8).toByte()
        writeBuffer[0] = v.toByte()
        out.write(writeBuffer, 0, 8)
    }

    @Throws(IOException::class)
    fun writeFloat(v: Float) {
        writeInt(v.toBits())
    }

    @Throws(IOException::class)
    fun writeDouble(v: Double) {
        writeLong(v.toBits())
    }

    @Throws(IOException::class)
    fun writeBoolean(v: Boolean) {
        out.write(if (v) 1 else 0)
    }

    @Throws(IOException::class)
    fun writeByte(v: Byte) {
        out.write(v.toInt())
    }

    @Throws(IOException::class)
    fun writeChar(v: Char) {
        out.write(v.code)
    }
}
