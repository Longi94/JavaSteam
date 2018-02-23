package in.dragonbra.javasteam.util.stream;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basically DataInputStream, but the bytes are parsed in reverse prder
 */
public class BinaryReader extends FilterInputStream {

    private byte readBuffer[] = new byte[8];

    public BinaryReader(InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public byte[] readBytes(int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        byte[] bytes = new byte[len];
        in.read(bytes);
        return bytes;
    }

    public byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte) ch;
    }

    public short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch2 << 8) + ch1);
    }

    public long readLong() throws IOException {
        in.read(readBuffer, 0, 8);
        return (((long) readBuffer[7] << 56) +
                ((long) (readBuffer[6] & 255) << 48) +
                ((long) (readBuffer[5] & 255) << 40) +
                ((long) (readBuffer[4] & 255) << 32) +
                ((long) (readBuffer[3] & 255) << 24) +
                ((readBuffer[2] & 255) << 16) +
                ((readBuffer[1] & 255) << 8) +
                (readBuffer[0] & 255));
    }

    public char readChar() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (char) ((ch2 << 8) + ch1);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public boolean readBoolean() throws IOException {
        int ch = in.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch != 0;
    }
}