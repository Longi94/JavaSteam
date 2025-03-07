package in.dragonbra.javasteam.util.stream;

import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Creates a stream whose backing store is memory.
 */
public class MemoryStream extends InputStream implements Closeable {
    private byte[] buffer;
    private int capacity;
    private int length;
    private final int origin;
    private int position;
    private final boolean writable;
    private final boolean expandable;
    private final boolean bufferVisible;
    private int mark;

    /**
     * Initializes a new instance of the MemoryStream class with an expandable capacity
     * initialized to zero.
     */
    public MemoryStream() {
        this(0);
    }

    /**
     * Initializes a new instance of the MemoryStream class with an expandable capacity
     * initialized as specified.
     *
     * @param capacity The initial size of the internal array in bytes.
     */
    public MemoryStream(int capacity) {
        this.buffer = new byte[capacity];

        this.capacity = capacity;
        this.length = 0;
        this.origin = 0;
        this.position = 0;

        this.writable = true;
        this.expandable = true;
        this.bufferVisible = true;
    }

    /**
     * Initializes a new non-resizable instance of the MemoryStream class based on the
     * specified byte array.
     *
     * @param byteString The sequence of bytes from which to create the current stream.
     */
    public MemoryStream(ByteString byteString) {
        this(byteString.toByteArray(), true);
    }


    /**
     * Initializes a new non-resizable instance of the MemoryStream class based on the
     * specified byte array.
     *
     * @param buffer The array of unsigned bytes from which to create the current stream.
     */
    public MemoryStream(byte[] buffer) {
        this(buffer, true);
    }

    /**
     * Initializes a new non-resizable instance of the MemoryStream class based on the
     * specified byte array, with writable set as specified.
     *
     * @param buffer   The array of unsigned bytes from which to create this stream.
     * @param writable True if the stream supports writing, false otherwise.
     */
    public MemoryStream(byte[] buffer, boolean writable) {
        this.buffer = buffer;

        this.capacity = buffer.length;
        this.length = buffer.length;
        this.origin = 0;
        this.position = 0;

        this.writable = writable;
        this.expandable = true;
        this.bufferVisible = false;
    }

    /**
     * Initializes a new non-resizable instance of the MemoryStream class based on the
     * specified region of a byte array.
     *
     * @param buffer The array of unsigned bytes from which to create this stream.
     * @param index  The index into buffer at which the stream begins.
     * @param count  The length of the stream in bytes.
     */
    public MemoryStream(byte[] buffer, int index, int count) {
        this(buffer, index, count, true, false);
    }

    /**
     * Initializes a new non-resizable instance of the MemoryStream class based on the
     * specified region of a byte array, with writable set as specified.
     *
     * @param buffer   The array of unsigned bytes from which to create this stream.
     * @param index    The index into buffer at which the stream begins.
     * @param count    The length of the stream in bytes.
     * @param writable True if the stream supports writing, false otherwise.
     */
    public MemoryStream(byte[] buffer, int index, int count, boolean writable) {
        this(buffer, index, count, writable, false);
    }

    /**
     * Initializes a new instance of the MemoryStream class based on the specified region
     * of a byte array, with writable set as specified, and publiclyVisible set as
     * specified.
     *
     * @param buffer          The array of unsigned bytes from which to create this stream.
     * @param index           The index into buffer at which the stream begins.
     * @param count           The length of the stream in bytes.
     * @param writable        Whether the stream supports writing.
     * @param publiclyVisible Whether the stream allows direct access to the underlying buffer.
     */
    public MemoryStream(byte[] buffer, int index, int count, boolean writable, boolean publiclyVisible) {
        this.buffer = buffer;

        this.capacity = index + count;
        this.length = this.capacity;
        this.origin = index;
        this.position = index;

        this.writable = writable;
        this.bufferVisible = publiclyVisible;
        this.expandable = false;
    }

    @Override
    public synchronized int available() {
        return length - position;
    }

    @Override
    public void close() {
    }

    @Override
    public void mark(int readAheadLimit) {
        mark = position;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() {
        position = mark;
    }

    @Override
    public synchronized int read() {
        if (position >= length)
            return -1;

        return buffer[position++] & 0xff;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        if (position >= length || len == 0)
            return 0;

        if (position > length - len)
            len = length - position;

        System.arraycopy(buffer, position, b, off, len);
        position += len;
        return len;
    }

    @Override
    public synchronized long skip(long n) {
        int previousPosition = position;
        long newPosition = seek(n, SeekOrigin.CURRENT);
        return newPosition - previousPosition;
    }

    /**
     * Returns the array of bytes from which this stream was created.
     *
     * @return byte[] The byte array from which this stream was created, or the underlying
     * array if a byte array was not provided to the MemoryStream constructor
     * during construction of the current instance.
     */
    public byte[] getBuffer() {
        if (!bufferVisible)
            throw new IllegalStateException("Cannot call getBuffer().");

        return buffer;
    }

    /**
     * Gets the number of bytes allocated for this stream.
     *
     * @return The length of the usable portion of the buffer for the stream.
     */
    public int getCapacity() {
        return capacity - origin;
    }

    /**
     * Sets the number of bytes allocated for this stream.
     *
     * @param value The new length of the usable portion of the buffer for the stream.
     */
    public void setCapacity(int value) {
        if (!expandable)
            throw new UnsupportedOperationException("Cannot expand this MemoryStream");

        if (buffer != null && value == buffer.length)
            return;

        byte[] newBuffer = null;
        if (value > 0) {
            newBuffer = new byte[value];
            if (buffer != null)
                System.arraycopy(buffer, 0, newBuffer, 0, length);
        }

        buffer = newBuffer;
        capacity = value;
    }

    /**
     * Gets the length of the stream in bytes.
     *
     * @return The length of the stream in bytes.
     */
    public long getLength() {
        return length - origin;
    }

    /**
     * Sets the length of this stream to the given value.
     *
     * @param value The new length of the stream.
     */
    public void setLength(long value) {
        if (value < 0)
            throw new IndexOutOfBoundsException("MemoryStream length must be non-negative.");
        if (!writable)
            throw new UnsupportedOperationException("Cannot write to this stream.");

        int newLength = (int) value + origin;

        boolean newArray = expand(newLength);
        if (!newArray && newLength > length) {
            clearBuffer(length, newLength);
        }

        length = newLength;
        if (position > length)
            position = length;
    }

    /**
     * Gets the current position within the stream.
     *
     * @return The current position within the stream.
     */
    public long getPosition() {
        return position - origin;
    }

    /**
     * Sets the current position within the stream.
     *
     * @param value The new position within the stream.
     */
    public void setPosition(long value) {
        position = origin + (int) value;
    }

    /**
     * Sets the position within the current stream to the specified value.
     *
     * @param offset The new position within the stream. This is relative to the loc
     *               parameter, and can be positive or negative.
     * @param loc    A value of type SeekOrigin, which acts as the seek reference point.
     * @return The new position within the stream, calculated by combining the initial
     * reference point and the offset.
     */
    public long seek(long offset, SeekOrigin loc) {
        int reference;
        switch (loc) {
            case BEGIN:
                reference = origin;
                break;
            case CURRENT:
                reference = position;
                break;
            case END:
                reference = length;
                break;
            default:
                throw new IllegalArgumentException("loc");
        }

        position = reference + (int) offset;
        return position;
    }

    /**
     * Writes a block of bytes to the current stream using data read from buffer.
     *
     * @param buffer The buffer to write data from.
     * @param offset The zero-based byte offset in buffer at which to begin copying bytes to
     *               the current stream.
     * @param count  The maximum number of bytes to write.
     */
    public void write(byte[] buffer, int offset, int count) {
        if (!writable)
            throw new UnsupportedOperationException("Cannot write to this stream.");

        int newPosition = position + count;
        if (newPosition > length) {
            boolean newArray = expand(newPosition);
            if (!newArray && position > length) {
                clearBuffer(length, newPosition);
            }
        }

        System.arraycopy(buffer, offset, this.buffer, position, count);
        position = newPosition;

        if (newPosition > length) {
            length = newPosition;
        }
    }

    public void writeByte(byte value) {
        if (!writable)
            throw new UnsupportedOperationException("Cannot write to this stream.");

        if (position >= length) {
            int newLength = position + 1;
            boolean newArray = expand(newLength);
            if (!newArray && position > length) {
                clearBuffer(length, position);
            }
            length = newLength;
        }
        buffer[position++] = value;
    }

    private void clearBuffer(int from, int to) {
        Arrays.fill(buffer, from, to, (byte) 0);
    }

    private boolean expand(int newLength) {
        if (newLength > capacity) {
            int newCapacity = Math.max(256, newLength);
            newCapacity = Math.max(capacity * 2, newCapacity);

            setCapacity(newCapacity);
            return true;
        }
        return false;
    }

    public byte[] toByteArray() {
        byte[] ret = new byte[length];
        System.arraycopy(buffer, 0, ret, 0, length);
        return ret;
    }

    @Override
    public byte @NotNull [] readAllBytes()  {
        return toByteArray();
    }

    /**
     * Get an OutputStream that will write to this MemoryStream, at the current position.
     *
     * @return an OutputStream that will write in-place to this MemoryStream.
     */
    public OutputStream asOutputStream() {
        return new OutputStreamWrapper(this);
    }

    private static class OutputStreamWrapper extends OutputStream {
        private final MemoryStream memoryStream;

        public OutputStreamWrapper(MemoryStream memoryStream) {
            this.memoryStream = memoryStream;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            memoryStream.write(b, off, len);
        }

        @Override
        public void write(int b) {
            memoryStream.writeByte((byte) b);
        }
    }
}
