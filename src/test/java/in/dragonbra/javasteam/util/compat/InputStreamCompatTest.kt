package `in`.dragonbra.javasteam.util.compat

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.random.Random

/**
 * [readNBytesCompat] methods are kotlin only extension functions for [InputStream].
 * This test is in Kotlin to ensure functionality for the extension functions
 *
 * @author Lossy
 * @since 30/12/2024
 */
class InputStreamCompatTest {

    private lateinit var testData: ByteArray
    private lateinit var emptyStream: InputStream
    private lateinit var normalStream: InputStream
    private lateinit var largeStream: InputStream

    @BeforeEach
    fun setup() {
        testData = Random.nextBytes(16384)
        emptyStream = ByteArrayInputStream(ByteArray(0))
        normalStream = ByteArrayInputStream(testData)
        largeStream = ByteArrayInputStream(Random.nextBytes(1024 * 1024))
    }

    @Nested
    inner class ReadNBytesCompatArrayTest {

        @Test
        fun `empty stream returns zero bytes`() {
            val buffer = ByteArray(100)
            val bytesRead = emptyStream.readNBytesCompat(buffer, 0, buffer.size)
            Assertions.assertEquals(0, bytesRead)
        }

        @Test
        fun `reading zero bytes returns zero`() {
            val buffer = ByteArray(100)
            val bytesRead = normalStream.readNBytesCompat(buffer, 0, 0)
            Assertions.assertEquals(0, bytesRead)
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 100, 1000, 8192])
        fun `reading n bytes matches JDK implementation`(n: Int) {
            val compatBuffer = ByteArray(n)
            val jdkBuffer = ByteArray(n)

            val stream1 = ByteArrayInputStream(testData)
            val stream2 = ByteArrayInputStream(testData)

            val compatBytesRead = stream1.readNBytesCompat(compatBuffer, 0, n)
            val jdkBytesRead = stream2.readNBytes(jdkBuffer, 0, n)

            Assertions.assertArrayEquals(jdkBuffer, compatBuffer)
            Assertions.assertEquals(jdkBytesRead, compatBytesRead)
        }

        @Test
        fun `throws exception on negative length`() {
            val buffer = ByteArray(100)
            Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
                normalStream.readNBytesCompat(buffer, 0, -1)
            }
        }

        @Test
        fun `throws exception on invalid offset`() {
            val buffer = ByteArray(100)
            Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
                normalStream.readNBytesCompat(buffer, -1, 50)
            }
            Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
                normalStream.readNBytesCompat(buffer, 101, 50)
            }
        }
    }

    @Nested
    inner class ReadNBytesCompatLengthTest {

        @Test
        fun `empty stream returns empty array`() {
            val result = emptyStream.readNBytesCompat(100)
            Assertions.assertEquals(0, result.size)
        }

        @Test
        fun `reading zero bytes returns empty array`() {
            val result = normalStream.readNBytesCompat(0)
            Assertions.assertEquals(0, result.size)
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 100, 1000, 8192, 16384])
        fun `reading n bytes matches JDK implementation`(n: Int) {
            val stream1 = ByteArrayInputStream(testData)
            val stream2 = ByteArrayInputStream(testData)

            val compatResult = stream1.readNBytesCompat(n)
            val jdkResult = stream2.readNBytes(n)

            Assertions.assertArrayEquals(jdkResult, compatResult)
        }

        @Test
        fun `throws exception on negative length`() {
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                normalStream.readNBytesCompat(-1)
            }
        }

        @Test
        fun `handles large reads correctly`() {
            val lengthToRead = 1024 * 1024
            val result = largeStream.readNBytesCompat(lengthToRead)
            Assertions.assertEquals(lengthToRead, result.size)
        }

        @Test
        fun `partial read when EOF reached`() {
            val result = normalStream.readNBytesCompat(testData.size * 2)
            Assertions.assertEquals(testData.size, result.size)
            Assertions.assertArrayEquals(testData, result)
        }
    }

    @Nested
    inner class ErrorConditionsTest {

        @Test
        fun `handles IOException from underlying stream`() {
            val failingStream = object : InputStream() {
                override fun read(): Int = throw IOException("Simulated failure")
                override fun read(b: ByteArray, off: Int, len: Int): Int = throw IOException("Simulated failure")
            }

            Assertions.assertThrows(IOException::class.java) {
                failingStream.readNBytesCompat(100)
            }

            Assertions.assertThrows(IOException::class.java) {
                failingStream.readNBytesCompat(ByteArray(100), 0, 100)
            }
        }
    }
}
