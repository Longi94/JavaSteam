package in.dragonbra.javasteam.util.stream;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BinaryReader {

    public static long LongMaxValue = 0xFFFFFFFFFFFFFFFFL;

    private CodedInputStream reader;
    private int len = 0;

    public BinaryReader(InputStream stream) {
        reader = CodedInputStream.newInstance(stream);
    }

    public BinaryReader(byte[] data) {
        reader = CodedInputStream.newInstance(data);
        len = data.length;
        try {
            reader.pushLimit(len);
        } catch (final InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public long readLong() throws IOException {
        return getBuffer(8).getLong();
    }

    private ByteBuffer getBuffer(int size) throws IOException {
        final byte[] buffer = new byte[size];
        for (int i = 1; i <= size; i++) {
            buffer[size - i] = reader.readRawByte();
        }
        return ByteBuffer.wrap(buffer);
    }

    public int readInt() throws IOException {
        return getBuffer(4).getInt();
    }

    public int getPosition() {
        return reader.getTotalBytesRead();
    }

    public int getRemaining() {
        return len - getPosition();
    }

    public CodedInputStream getStream() {
        return reader;
    }

    public short readShort() throws IOException {
        return getBuffer(2).getShort();
    }

    public byte readByte() throws IOException {
        return reader.readRawByte();
    }

    public byte[] readBytes(int length) throws IOException {
        return reader.readRawBytes(length);
    }

    public boolean isAtEnd() throws IOException {
        return reader.isAtEnd();
    }

    public byte[] readBytes() throws IOException {
        return reader.readRawBytes(getRemaining());
    }

    public float readFloat() throws IOException {
        return getBuffer(4).getFloat();
    }

    public String readString() throws IOException {
        return reader.readString();
    }

}
