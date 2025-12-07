package `in`.dragonbra.javasteam.base

import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.BinaryWriter
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import `in`.dragonbra.javasteam.util.stream.SeekOrigin
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * This class provides a payload backing to client messages.
 * @constructor Initializes a new instance of the [AbstractMsgBase] class.
 * @param payloadReserve The number of bytes to initialize the payload capacity to.
 */
@Suppress("unused")
abstract class AbstractMsgBase @JvmOverloads constructor(payloadReserve: Int = 0) {

    /**
     * Returns a [MemoryStream]which is the backing stream for client message payload data.
     */
    val payload: MemoryStream = MemoryStream(payloadReserve)

    private val reader: BinaryReader = BinaryReader(payload)

    private val writer: BinaryWriter = BinaryWriter(payload.asOutputStream())

    /**
     * Seeks within the payload to the specified offset.
     * @param offset     The offset in the payload to seek to.
     * @param seekOrigin The origin to seek from.
     * @return The new position within the stream, calculated by combining the initial reference point and the offset.
     */
    fun seek(offset: Long, seekOrigin: SeekOrigin): Long = payload.seek(offset, seekOrigin)

    /* Writers */

    /**
     * Writes a single [Byte] to the message payload.
     * @param data the byte.
     */
    @Throws(IOException::class)
    fun writeByte(data: Byte) {
        writer.write(data.toInt())
    }

    /**
     * Writes the specified [ByteArray] to the message payload.
     * @param data the byte array.
     */
    @Throws(IOException::class)
    fun writeBytes(data: ByteArray) {
        writer.write(data)
    }

    /**
     * Writes a single [Short] to the message payload.
     * @param data the short.
     */
    @Throws(IOException::class)
    fun writeShort(data: Short) {
        writer.writeShort(data)
    }

    /**
     * Writes a single [Int] to the message payload.
     * @param data the integer.
     */
    @Throws(IOException::class)
    fun writeInt(data: Int) {
        writer.writeInt(data)
    }

    /**
     * Writes a single [Long] to the message payload.
     * @param data the long.
     */
    @Throws(IOException::class)
    fun writeLong(data: Long) {
        writer.writeLong(data)
    }

    /**
     * Writes a single [Float] to the message payload.
     * @param data the float.
     */
    @Throws(IOException::class)
    fun writeFloat(data: Float) {
        writer.writeFloat(data)
    }

    /**
     * Writes a single [Double] to the message payload.
     * @param data the double.
     */
    @Throws(IOException::class)
    fun writeDouble(data: Double) {
        writer.writeDouble(data)
    }

    /**
     * Writes the specified string to the message payload using UTF-8 encoding.
     * This function does not write a terminating null character.
     * @param data The string to write.
     * @param charset The encoding to use.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun writeString(data: String?, charset: Charset = StandardCharsets.UTF_8) {
        if (data == null) {
            return
        }

        writeBytes(data.toByteArray(charset))
    }

    /**
     * Writes the specified string and a null terminator to the message payload using UTF-8 encoding.
     * Default encoding is UTF-8
     * @param data The string to write.
     * @param charset The encoding to use.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun writeNullTermString(data: String?, charset: Charset = StandardCharsets.UTF_8) {
        writeString(data, charset)
        writeString("\u0000", charset)
    }

    /* Readers */

    /**
     * Reads a single [Byte] from the message payload.
     * @return The byte.
     */
    @Throws(IOException::class)
    fun readByte(): Byte = reader.readByte()

    /**
     * Reads a number of bytes from the message payload.
     * @param numBytes The number of bytes to read.
     * @return The data.
     */
    @Throws(IOException::class)
    fun readBytes(numBytes: Int): ByteArray = reader.readBytes(numBytes)

    /**
     * Reads a single [Short] from the message payload.
     * @return The short.
     */
    @Throws(IOException::class)
    fun readShort(): Short = reader.readShort()

    /**
     * Reads a single [Int] from the message payload.
     * @return The integer.
     */
    @Throws(IOException::class)
    fun readInt(): Int = reader.readInt()

    /**
     * Reads a single [Long] from the message payload.
     * @return The long.
     */
    @Throws(IOException::class)
    fun readLong(): Long = reader.readLong()

    /**
     * Reads a single [Float] from the message payload.
     * @return The float.
     */
    @Throws(IOException::class)
    fun readFloat(): Float = reader.readFloat()

    /**
     * Reads a single [Double] from the message payload.
     * @return The double.
     */
    @Throws(IOException::class)
    fun readDouble(): Double = reader.readDouble()

    /**
     * Reads a null terminated string from the message payload with the specified encoding.
     * Default encoding is UTF-8
     * @param charset The encoding to use.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun readNullTermString(charset: Charset = StandardCharsets.UTF_8): String = reader.readNullTermString(charset)
}
