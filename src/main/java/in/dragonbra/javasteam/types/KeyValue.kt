package `in`.dragonbra.javasteam.types

import `in`.dragonbra.javasteam.util.Passable
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Represents a recursive string key to arbitrary value container.
 * @constructor Initializes a new instance of the <see cref="KeyValue"/> class.
 * @param name The optional name of the root key.
 * @param value The optional value assigned to the root key.
 * @property name Gets or sets the name of this instance.
 * @property value Gets or sets the value of this instance.
 */
@Suppress("unused")
class KeyValue @JvmOverloads constructor(
    var name: String? = null,
    var value: String? = null,
) {

    /**
     * Gets the children of this instance.
     */
    var children: MutableList<KeyValue> = ArrayList(4) // Give an initial capacity for optimization.

    /**
     * Gets the child [KeyValue] with the specified key.
     * If no child with the given key exists, [KeyValue.INVALID] is returned.
     * @param key key
     * @return the child [KeyValue]
     */
    operator fun get(key: String): KeyValue = children.find {
        it.name?.equals(key, ignoreCase = true) == true
    } ?: INVALID

    /**
     * Sets the child [KeyValue] with the specified key.
     * If no child with the given key exists, [KeyValue.INVALID] is returned.
     * @param key key
     * @param value the child [KeyValue]
     */
    operator fun set(key: String, value: KeyValue) {
        // if the key already exists, remove the old one
        children.removeIf { c -> c.name?.equals(key, ignoreCase = true) == true }

        // Ensure the given KV has the correct key assigned
        value.name = key

        children.add(value)
    }

    /**
     * Returns the value of this instance as a string.
     */
    fun asString(): String? = this.value

    /**
     * Attempts to convert and return the value of this instance as a byte.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as a byte.
     */
    @JvmOverloads
    fun asByte(defaultValue: Byte = 0): Byte = value?.toByteOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as an unsigned byte.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    @JvmOverloads
    fun asUnsignedByte(defaultValue: UByte = 0u): UByte = value?.toUByteOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as a short.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as a short.
     */
    @JvmOverloads
    fun asShort(defaultValue: Short = 0): Short = value?.toShortOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as an unsigned short.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned short.
     */
    @JvmOverloads
    fun asUnsignedShort(defaultValue: UShort = 0u): UShort = value?.toUShortOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as an integer.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an integer.
     */
    @JvmOverloads
    fun asInteger(defaultValue: Int = 0): Int = value?.toIntOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as an unsigned integer.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned integer.
     */
    @JvmOverloads
    fun asUnsignedInteger(defaultValue: UInt = 0u): UInt = value?.toUIntOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as a long.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as a long.
     */
    @JvmOverloads
    fun asLong(defaultValue: Long = 0L): Long = value?.toLongOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as an unsigned long.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned long.
     */
    @JvmOverloads
    fun asUnsignedLong(defaultValue: ULong = 0uL): ULong = value?.toULongOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as a float.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as a float.
     */
    @JvmOverloads
    fun asFloat(defaultValue: Float = 0f): Float = value?.toFloatOrNull() ?: defaultValue

    /**
     * Attempts to convert and return the value of this instance as a boolean.
     * If the conversion is invalid, the default value is returned.
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as a boolean.
     */
    @JvmOverloads
    fun asBoolean(defaultValue: Boolean = false): Boolean = try {
        value!!.toInt() != 0
    } catch (e: Exception) {
        when (value?.lowercase()) {
            "true" -> true
            "false" -> false
            else -> defaultValue
        }
    }

    /**
     * Attempts to convert and return the value of this instance as an enum.
     * If the conversion is invalid, the default value is returned.
     * @param T The type of the enum to convert to
     * @param enumClass The type of the enum to convert to
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    fun <T : Enum<T>> asEnum(enumClass: Class<T>, defaultValue: T): EnumSet<T> =
        asEnum(enumClass, EnumSet.of(defaultValue))

    /**
     * Attempts to convert and return the value of this instance as an enum.
     * If the conversion is invalid, the default value is returned.
     * @param T The type of the enum to convert to
     * @param enumClass The type of the enum to convert to
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Enum<T>> asEnum(enumClass: Class<T>, defaultValue: EnumSet<T>): EnumSet<T> {
        // this is ugly af, but it comes with handling bit flags as enumsets
        try {
            // see if it's a number first
            val code = value?.toInt() ?: return defaultValue

            val codeField = enumClass.getDeclaredField("code")
            val fromMethod = enumClass.getMethod("from", codeField.type)

            @Suppress("MoveVariableDeclarationIntoWhen")
            val result = fromMethod.invoke(null, code)

            return when (result) {
                is EnumSet<*> -> result as EnumSet<T>
                else -> EnumSet.of(enumClass.cast(result))
            }
        } catch (e: NumberFormatException) {
            // ignore and try next approach
        } catch (e: NoSuchFieldException) {
            return defaultValue
        } catch (e: NoSuchMethodException) {
            return defaultValue
        } catch (e: IllegalAccessException) {
            return defaultValue
        } catch (e: InvocationTargetException) {
            return defaultValue
        }

        try {
            // see if it exists as an enum
            val enumValue = java.lang.Enum.valueOf(enumClass, value ?: return defaultValue)
            return EnumSet.of(enumValue)
        } catch (e: IllegalArgumentException) {
            // ignore and try next approach
        }

        // check for static enumset fields
        try {
            for (field in enumClass.declaredFields) {
                if (Modifier.isStatic(field.modifiers) &&
                    field.name == value &&
                    EnumSet::class.java.isAssignableFrom(field.type)
                ) {
                    @Suppress("UNCHECKED_CAST")
                    return field.get(null) as EnumSet<T>
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return defaultValue
    }

    /**
     * Returns a [String] that represents this instance.
     */
    override fun toString(): String = "$name = $value"

    /**
     * Populate this instance from the given [InputStream] as a text [KeyValue].
     * @param input The input [InputStream] to read from.
     * @return <c>true</c> if the read was successful otherwise, <c>false</c>.
     */
    fun readAsText(input: InputStream): Boolean {
        children.clear()

        KVTextReader(this, input).use { _ -> }

        return true
    }

    /**
     * Opens and reads the given filename as text.
     * @see [readAsText]
     * @param filename The file to open and read.
     * @return <c>true</c> if the read was successful otherwise, <c>false</c>.
     */
    fun readFileAsText(filename: String): Boolean = FileInputStream(filename).use(::readAsText)

    internal fun recursiveLoadFromBuffer(kvr: KVTextReader) {
        val wasQuoted = Passable(false)
        val wasConditional = Passable(false)

        while (true) {
            // val bAccepted = true

            // get the key name
            val name = kvr.readToken(wasQuoted = wasQuoted, wasConditional = wasConditional)

            if (name.isNullOrEmpty()) {
                throw IllegalStateException("RecursiveLoadFromBuffer: got EOF or empty keyname")
            }

            if (name.startsWith('}') && wasQuoted.value == false) {
                // top level closed, stop reading
                break
            }

            val dat = KeyValue(name)
            dat.children.clear()
            this.children.add(dat)

            // get the value
            var value = kvr.readToken(wasQuoted, wasConditional)

            if (wasConditional.value == true && value != null) {
                // bAccepted = ( value == "[$WIN32]" )
                value = kvr.readToken(wasQuoted, wasConditional)
            }

            if (value == null) {
                throw IllegalStateException("RecursiveLoadFromBuffer: got NULL key")
            }

            if (value.startsWith('}') && wasQuoted.value == false) {
                throw IllegalStateException("RecursiveLoadFromBuffer: got } in key")
            }

            if (value.startsWith('{') && wasQuoted.value == false) {
                dat.recursiveLoadFromBuffer(kvr)
            } else {
                if (wasConditional.value == true) {
                    throw IllegalStateException("RecursiveLoadFromBuffer: got conditional between key and value")
                }

                dat.value = value
                // blahconditionalsdontcare
            }
        }
    }

    /**
     * Saves this instance to file.
     * @param file The file to save to.
     * @param asBinary If set to <c>true</c>, saves this instance as binary.
     */
    fun saveToFile(file: File, asBinary: Boolean) {
        FileOutputStream(file, false).use { f -> saveToStream(f, asBinary) }
    }

    /**
     * Saves this instance to file.
     * @param path The file path to save to.
     * @param asBinary If set to <c>true</c>, saves this instance as binary.
     */
    fun saveToFile(path: String, asBinary: Boolean) {
        FileOutputStream(path, false).use { f -> saveToStream(f, asBinary) }
    }

    /**
     * Saves this instance to a given [OutputStream]
     * @param stream The [OutputStream] to save to.
     * @param asBinary If set to <c>true</c>, saves this instance as binary.
     */
    @Throws(IOException::class)
    fun saveToStream(stream: OutputStream, asBinary: Boolean) {
        if (asBinary) {
            recursiveSaveBinaryToStream(stream)
        } else {
            recursiveSaveTextToFile(stream)
        }
    }

    @Throws(IOException::class)
    private fun recursiveSaveBinaryToStream(f: OutputStream) {
        recursiveSaveBinaryToStreamCore(f)
        f.write(Type.END.code.toInt())
    }

    @Throws(IOException::class)
    private fun recursiveSaveBinaryToStreamCore(f: OutputStream) {
        // Only supported types ATM:
        // 1. KeyValue with children (no value itself)
        // 2. String KeyValue
        if (value == null) {
            f.write(Type.NONE.code.toInt())
            f.write(getNameForSerialization().toByteArray(StandardCharsets.UTF_8))
            f.write(0)
            children.forEach { child ->
                child.recursiveSaveBinaryToStreamCore(f)
            }
            f.write(Type.END.code.toInt())
        } else {
            f.write(Type.STRING.code.toInt())
            f.write(getNameForSerialization().toByteArray(StandardCharsets.UTF_8))
            f.write(0)
            f.write(value?.toByteArray(StandardCharsets.UTF_8))
            f.write(0)
        }
    }

    private fun recursiveSaveTextToFile(os: OutputStream, indentLevel: Int = 0) {
        // write header
        writeIndents(os, indentLevel)
        writeString(os, getNameForSerialization(), true)
        writeString(os, "\n")
        writeIndents(os, indentLevel)
        writeString(os, "{\n")

        // loop through all our keys writing them to disk
        children.forEach { child ->
            if (child.value == null) {
                child.recursiveSaveTextToFile(os, indentLevel + 1)
            } else {
                writeIndents(os, indentLevel + 1)
                writeString(os, child.getNameForSerialization(), true)
                writeString(os, "\t\t")
                writeString(os, escapeText(child.asString()!!), true)
                writeString(os, "\n")
            }
        }

        writeIndents(os, indentLevel)
        writeString(os, "}\n")
    }

    /**
     * Populate this instance from the given [InputStream] as a binary [KeyValue].
     * @param input The input [InputStream] to read from.
     * @return <c>true</c> if the read was successful otherwise, <c>false</c>.
     */
    @Throws(IOException::class, EOFException::class)
    fun tryReadAsBinary(input: InputStream): Boolean = BinaryReader(input).use { br ->
        tryReadAsBinaryCore(br, this, null)
    }

    private fun getNameForSerialization(): String = requireNotNull(name) {
        "Cannot serialise a KeyValue object with a null name!"
    }

    companion object {

        private val logger: Logger = LogManager.getLogger<KeyValue>()

        enum class Type(val code: Byte) {
            NONE(0),
            STRING(1),
            INT32(2),
            FLOAT32(3),
            POINTER(4),
            WIDESTRING(5),
            COLOR(6),
            UINT64(7),
            END(8),
            INT64(10),
            ALTERNATEEND(11),
            ;

            companion object {
                private val codeMap = entries.associateBy { it.code }

                @JvmStatic
                fun from(code: Byte): Type? = codeMap[code]
            }
        }

        /**
         * Represents an invalid [KeyValue] given when a searched for child does not exist.
         */
        @JvmField
        val INVALID = KeyValue()

        /**
         * Attempts to load the given filename as a text [KeyValue].
         * This method will swallow any exceptions that occur when reading, use [readAsText] if you wish to handle exceptions.
         * @param path The path to the file to load.
         * @return a [KeyValue] instance if the load was successful, or <c>null</c> on failure.
         */
        @JvmStatic
        fun loadAsText(path: String): KeyValue? = loadFromFile(path, false)

        /**
         * Attempts to load the given filename as a binary <see cref="KeyValue"/>.
         * @param path The path to the file to load.
         * @return The resulting [KeyValue] object if the load was successful, or <c>null</c> if unsuccessful.
         */
        @JvmStatic
        fun tryLoadAsBinary(path: String): KeyValue? = loadFromFile(path, true)

        private fun loadFromFile(path: String, asBinary: Boolean): KeyValue? {
            val file = File(path)

            if (!file.exists() || file.isDirectory()) {
                return null
            }

            try {
                FileInputStream(file).use { input ->
                    val kv = KeyValue()

                    if (asBinary) {
                        if (!kv.tryReadAsBinary(input)) {
                            return null
                        }
                    } else {
                        if (!kv.readAsText(input)) {
                            return null
                        }
                    }

                    return kv
                }
            } catch (e: Exception) {
                logger.error(e.message, e)
                return null
            }
        }

        /**
         * Attempts to create an instance of [KeyValue] from the given input text.
         * This method will swallow any exceptions that occur when reading, use [readAsText] if you wish to handle exceptions.
         * @param input The input text to load.
         * @return a [KeyValue] instance if the load was successful, or <c>null</c> on failure.
         */
        @JvmStatic
        fun loadFromString(input: String): KeyValue? {
            val bytes = input.toByteArray(StandardCharsets.UTF_8)

            try {
                ByteArrayInputStream(bytes).use { stream ->
                    val kv = KeyValue()

                    if (!kv.readAsText(stream)) {
                        return null
                    }
                    return kv
                }
            } catch (e: Exception) {
                logger.error(e.message, e)
                return null
            }
        }

        private fun escapeText(value: String): String {
            var localValue = value
            KVTextReader.ESCAPED_MAPPING.forEach { kvp ->
                val textToReplace = kvp.value.toString()
                val escapedReplacement = "\\" + kvp.key
                localValue = localValue.replace(textToReplace, escapedReplacement)
            }
            return localValue
        }

        private fun writeIndents(stream: OutputStream, indentLevel: Int) {
            writeString(stream, "\t".repeat(indentLevel))
        }

        private fun writeString(stream: OutputStream, str: String, quote: Boolean = false) {
            val processedStr = str.replace("\"", "\\\"")
            val finalStr = if (quote) "\"$processedStr\"" else processedStr
            val bytes = finalStr.toByteArray(Charsets.UTF_8)
            stream.write(bytes)
        }

        private fun tryReadAsBinaryCore(input: BinaryReader, current: KeyValue, parent: KeyValue?): Boolean {
            var localCurrent = current

            localCurrent.children.clear()

            while (true) {
                val type = Type.from(input.readByte())

                if (type == Type.END || type == Type.ALTERNATEEND) {
                    break
                }

                localCurrent.name = input.readNullTermString(StandardCharsets.UTF_8)

                when (type) {
                    Type.NONE -> {
                        val child = KeyValue()
                        val didReadChild = tryReadAsBinaryCore(input, child, localCurrent)
                        if (!didReadChild) {
                            return false
                        }
                    }

                    Type.STRING -> localCurrent.value = input.readNullTermString(StandardCharsets.UTF_8)

                    Type.WIDESTRING -> {
                        logger.debug("Encountered WideString type when parsing binary KeyValue, which is unsupported. Returning false.")
                        return false
                    }

                    Type.INT32,
                    Type.COLOR,
                    Type.POINTER,
                    -> localCurrent.value = input.readInt().toString()

                    Type.UINT64 -> localCurrent.value = input.readLong().toString()

                    Type.FLOAT32 -> localCurrent.value = input.readFloat().toString()

                    Type.INT64 -> localCurrent.value = input.readLong().toString()

                    else -> return false
                }

                parent?.children?.add(localCurrent)

                localCurrent = KeyValue()
            }

            return true
        }
    }
}
