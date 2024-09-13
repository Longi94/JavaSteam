package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This class provides a payload backing to client messages.
 */
@SuppressWarnings("unused")
public abstract class AbstractMsgBase {

    protected MemoryStream payload;

    private final BinaryReader reader;

    private final BinaryWriter writer;

    /**
     * Initializes a new instance of the {@link AbstractMsgBase} class.
     */
    public AbstractMsgBase() {
        this(0);
    }

    /**
     * Initializes a new instance of the {@link AbstractMsgBase} class.
     *
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public AbstractMsgBase(int payloadReserve) {
        payload = new MemoryStream(payloadReserve);

        reader = new BinaryReader(payload);
        writer = new BinaryWriter(payload.asOutputStream());
    }

    /**
     * Seeks within the payload to the specified offset.
     *
     * @param offset     The offset in the payload to seek to.
     * @param seekOrigin The origin to seek from.
     * @return The new position within the stream, calculated by combining the initial reference point and the offset.
     */
    public long seek(long offset, SeekOrigin seekOrigin) {
        return payload.seek(offset, seekOrigin);
    }

    public void writeByte(byte data) throws IOException {
        writer.write(data);
    }

    public byte readByte() throws IOException {
        return reader.readByte();
    }

    public void writeBytes(byte[] data) throws IOException {
        writer.write(data);
    }

    public byte[] readBytes(int numBytes) throws IOException {
        return reader.readBytes(numBytes);
    }

    public void writeShort(short data) throws IOException {
        writer.writeShort(data);
    }

    public short readShort() throws IOException {
        return reader.readShort();
    }

    public void writeInt(int data) throws IOException {
        writer.writeInt(data);
    }

    public int readInt() throws IOException {
        return reader.readInt();
    }

    public void writeLong(long data) throws IOException {
        writer.writeLong(data);
    }

    public long readLong() throws IOException {
        return reader.readLong();
    }

    public void writeFloat(float data) throws IOException {
        writer.writeFloat(data);
    }

    public float readFloat() throws IOException {
        return reader.readFloat();
    }

    public void writeDouble(double data) throws IOException {
        writer.writeDouble(data);
    }

    public double readDouble() throws IOException {
        return reader.readDouble();
    }

    public void writeString(String data) throws IOException {
        writeString(data, Charset.defaultCharset());
    }

    public void writeString(String data, Charset charset) throws IOException {
        if (data == null) {
            return;
        }

        if (charset == null) {
            throw new IllegalArgumentException("charset is null");
        }

        writeBytes(data.getBytes(charset));
    }

    public void writeNullTermString(String data) throws IOException {
        writeNullTermString(data, Charset.defaultCharset());
    }

    public void writeNullTermString(String data, Charset charset) throws IOException {
        writeString(data, charset);
        writeString("\0", charset);
    }

    public String readNullTermString() throws IOException {
        return readNullTermString(Charset.defaultCharset());
    }

    public String readNullTermString(Charset charset) throws IOException {
        return reader.readNullTermString(charset);
    }

    public MemoryStream getPayload() {
        return payload;
    }
}
