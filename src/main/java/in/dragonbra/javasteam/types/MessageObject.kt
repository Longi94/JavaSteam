package in.dragonbra.javasteam.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a {@link KeyValue} backed MessageObject structure, which are often sent by the Steam servers.
 */
public class MessageObject {

    protected KeyValue keyValues;

    /**
     * Initializes a new instance of the {@link MessageObject} class, using the provided KeyValues object.
     *
     * @param keyValues The KeyValue backing store for this message object.
     */
    public MessageObject(KeyValue keyValues) {
        if (keyValues == null) {
            throw new IllegalArgumentException("keyValues is null");
        }

        this.keyValues = keyValues;
    }

    /**
     * Initializes a new instance of the {@link MessageObject} class with an empty inner KeyValues.
     */
    public MessageObject() {
        this(new KeyValue("MessagObject"));
    }

    /**
     * Populates this MessageObject instance from the data inside the given stream.
     *
     * @param stream The stream to load data from.
     * @return <b>true</b> on success; otherwise, <b>false</b>.
     * @throws IOException IO exception during reading from the stream
     */
    public boolean readFromStream(InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream is null");
        }

        return keyValues.tryReadAsBinary(stream);
    }

    /**
     * Writes this MessageObject instance to the given stream.
     *
     * @param stream The stream to write to.
     * @throws IOException IO exception during writing to the stream
     */
    public void writeToStream(OutputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream is null");
        }

        keyValues.saveToStream(stream, true);
    }

}
