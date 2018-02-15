package in.dragonbra.javasteam.util.stream;

import com.google.protobuf.CodedOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BinaryWriter {

    private CodedOutputStream writer;
    private OutputStream os;
    private ByteArrayOutputStream stream = null;

    public BinaryWriter(ByteArrayOutputStream stream) {
        this((OutputStream) stream);
        this.stream = stream;
    }

    public BinaryWriter(int size) {
        this(new ByteArrayOutputStream(size));
    }

    public BinaryWriter() {
        this(32);
    }

    public BinaryWriter(OutputStream outputStream) {
        os = outputStream;
        writer = CodedOutputStream.newInstance(outputStream);
    }

    public void write(short data) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(data);
        writeR(buffer);
    }

    public void write(int data) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(data);
        writeR(buffer);
    }

    public void write(long data) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(data);
        writeR(buffer);
    }

    public byte[] toByteArray() {
        if (stream != null) {
            return stream.toByteArray();
        }
        return null;
    }

    public void writeR(ByteBuffer buffer) throws IOException {
        for (int i = buffer.capacity() - 1; i >= 0; --i) {
            write(buffer.get(i));
        }
    }

    public void write(byte[] data) throws IOException {
        writer.writeRawBytes(data);
        writer.flush();
    }

    public void write(byte data) throws IOException {
        writer.writeRawByte(data);
        writer.flush();
    }

    public CodedOutputStream getStream() {
        return writer;
    }

    public void flush() {
        try {
            os.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
