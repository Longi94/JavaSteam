package `in`.dragonbra.javasteam.types

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Represents a [KeyValue] backed MessageObject structure, which are often sent by the Steam servers.
 * @constructor Initializes a new instance of the [MessageObject] class, using the provided KeyValues object.
 * @param keyValues The KeyValue backing store for this message object.
 */
open class MessageObject @JvmOverloads constructor(
    protected var keyValues: KeyValue = KeyValue("MessageObject"),
) {
    /**
     * Populates this MessageObject instance from the data inside the given stream.
     * @param stream The stream to load data from.
     * @return **true** on success; otherwise, **false**.
     * @throws IOException IO exception during reading from the stream
     */
    @Throws(IOException::class)
    fun readFromStream(stream: InputStream): Boolean = keyValues.tryReadAsBinary(stream)

    /**
     * Writes this MessageObject instance to the given stream.
     * @param stream The stream to write to.
     * @throws IOException IO exception during writing to the stream
     */
    @Throws(IOException::class)
    fun writeToStream(stream: OutputStream) {
        keyValues.saveToStream(stream, true)
    }
}
