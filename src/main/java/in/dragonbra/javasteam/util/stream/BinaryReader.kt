package `in`.dragonbra.javasteam.util.stream

import `in`.dragonbra.javasteam.util.compat.ByteArrayOutputStreamCompat
import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Basically DataInputStream, but the bytes are parsed in reverse order
 */
class BinaryReader(inputStream: InputStream) : FilterInputStream(inputStream) {

    private val readBuffer = ByteArray(16)

    var position: Int = 0
        private set

    @Throws(IOException::class)
    fun readInt(): Int {
        val bytesRead = `in`.read(readBuffer, 0, 4)

        if (bytesRead != 4) {
            throw EOFException()
        }

        position += 4

        return ((readBuffer[3].toInt() and 0xFF) shl 24) or
            ((readBuffer[2].toInt() and 0xFF) shl 16) or
            ((readBuffer[1].toInt() and 0xFF) shl 8) or
            (readBuffer[0].toInt() and 0xFF)
    }

    @Throws(IOException::class)
    fun readBytes(len: Int): ByteArray {
        if (len < 0) {
            throw IOException("negative length")
        }

        val bytes = `in`.readNBytesCompat(len)

        if (bytes.size != len) {
            throw EOFException("Unexpected end of stream")
        }

        position += len

        return bytes
    }

    @Throws(IOException::class)
    fun readByte(): Byte {
        val ch = `in`.read()

        if (ch < 0) {
            throw EOFException()
        }

        position += 1

        return ch.toByte()
    }

    @Throws(IOException::class)
    fun readShort(): Short {
        val bytesRead = `in`.read(readBuffer, 0, 2)
        if (bytesRead != 2) {
            throw EOFException()
        }

        position += 2

        return (((readBuffer[1].toInt() and 0xFF) shl 8) or (readBuffer[0].toInt() and 0xFF)).toShort()
    }

    @Throws(IOException::class)
    fun readLong(): Long {
        val bytesRead = `in`.read(readBuffer, 0, 8)

        if (bytesRead != 8) {
            throw EOFException()
        }

        position += 8

        return (
            (readBuffer[7].toLong() shl 56) +
                ((readBuffer[6].toInt() and 255).toLong() shl 48) +
                ((readBuffer[5].toInt() and 255).toLong() shl 40) +
                ((readBuffer[4].toInt() and 255).toLong() shl 32) +
                ((readBuffer[3].toInt() and 255).toLong() shl 24) +
                ((readBuffer[2].toInt() and 255) shl 16) +
                ((readBuffer[1].toInt() and 255) shl 8) +
                (readBuffer[0].toInt() and 255)
            )
    }

    @Throws(IOException::class)
    fun readChar(): Char {
        val ch1 = `in`.read()

        if (ch1 < 0) {
            throw EOFException()
        }

        position += 1

        return ch1.toChar()
    }

    @Throws(IOException::class)
    fun readFloat(): Float = Float.fromBits(readInt())

    @Throws(IOException::class)
    fun readDouble(): Double = Double.fromBits(readLong())

    @Throws(IOException::class)
    fun readBoolean(): Boolean {
        val ch = `in`.read()

        if (ch < 0) {
            throw EOFException()
        }

        position += 1

        return ch != 0
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readNullTermString(charset: Charset = StandardCharsets.UTF_8): String {
        if (charset == StandardCharsets.UTF_8) {
            return readNullTermUtf8String()
        }

        val buffer = ByteArrayOutputStream(0)
        val bw = BinaryWriter(buffer)

        while (true) {
            val ch = readChar()

            if (ch.code == 0) {
                break
            }

            bw.writeChar(ch)
        }

        val bytes = buffer.toByteArray()

        position += bytes.size

        return String(bytes, charset)
    }

    @Throws(IOException::class)
    private fun readNullTermUtf8String(): String {
        val baos = ByteArrayOutputStream()

        while (true) {
            val b = `in`.read()

            if (b <= 0) {
                break
            }

            baos.write(b)
            position++
        }

        position++ // Increment for the null terminator

        return ByteArrayOutputStreamCompat.toString(baos)
    }
}
