package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.util.Passable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

/**
 * @author lngtr
 * @since 2018-02-26
 */
class KVTextReader
@Throws(IllegalStateException::class, IOException::class)
internal constructor(
    kv: KeyValue,
    inputStream: InputStream,
) : InputStreamReader(inputStream, StandardCharsets.UTF_8) {

    companion object {
        @JvmField
        val ESCAPED_MAPPING = mapOf<Char, Char>(
            '\\' to '\\',
            'n' to '\n',
            'r' to '\r',
            't' to '\t',
            // todo: (SK) any others?
        )
    }

    private val sb = StringBuilder(128)

    private var peekedChar: Int? = null

    // Mimics C# StreamReader 'Peek()'
    val peek: Int
        get() {
            if (peekedChar == null) {
                peekedChar = read()
            }
            return peekedChar ?: -1
        }

    // Mimics C# StreamReader 'EndOfStream'
    val endOfStream: Boolean
        get() {
            return try {
                peek == -1
            } catch (_: IOException) {
                true
            }
        }

    init {
        val wasQuoted = Passable(false)
        val wasConditional = Passable(false)

        var currentKey: KeyValue? = kv

        do {
            var s = readToken(wasQuoted, wasConditional)

            if (s.isNullOrEmpty()) {
                break
            }

            if (currentKey == null) {
                currentKey = KeyValue(s)
            } else {
                currentKey.name = s
            }

            s = readToken(wasQuoted, wasConditional)

            if (wasConditional.value == true) {
                // Now get the '{'
                s = readToken(wasQuoted, wasConditional)
            }

            if (s != null && s.startsWith("{") && wasQuoted.value == false) {
                // header is valid so load the file
                currentKey.recursiveLoadFromBuffer(this)
            } else {
                throw IllegalStateException("LoadFromBuffer: missing {")
            }

            currentKey = null
        } while (!endOfStream)
    }

    // override read() to peek the char.
    override fun read(): Int {
        if (peekedChar != null) {
            val result = peekedChar!!
            peekedChar = null
            return result
        }
        return super.read()
    }

    @Throws(IOException::class)
    private fun eatWhiteSpace() {
        while (!endOfStream) {
            if (!peek.toChar().isWhitespace()) {
                break
            }

            read()
        }
    }

    @Throws(IOException::class)
    private fun eatCPPComment(): Boolean {
        if (!endOfStream) {
            val next = peek.toChar()

            if (next == '/') {
                readLine()
                return true
                /*
                 *  As came up in parsing the Dota 2 units.txt file, the reference (Valve) implementation
                 *  of the KV format considers a single forward slash to be sufficient to comment out the
                 *  entirety of a line. While they still _tend_ to use two, it's not required, and likely
                 *  is just done out of habit.
                 */
            }

            return false
        }

        return false
    }

    @Throws(IOException::class)
    private fun readLine() {
        var c: Char
        do {
            c = read().toChar()
        } while (c != '\n' && !endOfStream)
    }

    @Throws(IOException::class)
    fun readToken(wasQuoted: Passable<Boolean>, wasConditional: Passable<Boolean>): String? {
        wasQuoted.value = false
        wasConditional.value = false

        while (true) {
            eatWhiteSpace()

            if (endOfStream) {
                return null
            }

            if (!eatCPPComment()) {
                break
            }
        }

        if (endOfStream) {
            return null
        }

        var next = peek.toChar()
        if (next == '"') {
            wasQuoted.value = true

            // "
            read()

            sb.clear()
            while (!endOfStream) {
                if (peek.toChar() == '\\') {
                    read()

                    val escapedChar = read().toChar()
                    val replacedChar = ESCAPED_MAPPING[escapedChar] ?: escapedChar

                    sb.append(replacedChar)

                    continue
                }

                if (peek.toChar() == '"') {
                    break
                }

                sb.append(read().toChar())
            }

            // "
            read()

            return sb.toString()
        }

        if (next == '{' || next == '}') {
            read()
            return next.toString()
        }

        var bConditionalStart = false
        val count = 0
        sb.clear()
        while (!endOfStream) {
            next = peek.toChar()

            if (next == '"' || next == '{' || next == '}') {
                break
            }

            if (next == '[') {
                bConditionalStart = true
            }

            if (next == ']' && bConditionalStart) {
                wasConditional.value = true
            }

            if (next.isWhitespace()) {
                break
            }

            // count isn't used anymore, but still defined in SK.
            @Suppress("KotlinConstantConditions")
            if (count < 1023) {
                sb.append(next)
            } else {
                throw IOException("ReadToken overflow")
            }

            read()
        }

        return sb.toString()
    }
}
