package in.dragonbra.javasteam.util.stream;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Basically DataInputStream, but the bytes are parsed in reverse order
 */
public class BinaryReader extends FilterInputStream {

    private final byte[] readBuffer = new byte[8];

    private int position = 0;

    public BinaryReader(InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        position += 4;
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }

    public byte[] readBytes(int len) throws IOException {
        if (len < 0) {
            throw new IOException("negative length");
        }

        byte[] bytes = new byte[len];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = readByte();
        }

        return bytes;
    }

    public byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0) {
            throw new EOFException();
        }
        position += 1;
        return (byte) ch;
    }

    public short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        position += 2;
        return (short) ((ch2 << 8) + ch1);
    }

    public long readLong() throws IOException {
        in.read(readBuffer, 0, 8);
        position += 8;
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
        if (ch1 < 0) {
            throw new EOFException();
        }
        position += 1;
        return (char) ch1;
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
        position += 1;
        return ch != 0;
    }
    public String readNullTermString() throws IOException {
        return readNullTermString(Charset.defaultCharset());
    }

    public String readNullTermString(Charset charset) throws IOException {
        if (charset == null) {
            throw new IOException("charset is null");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(0);
        BinaryWriter bw = new BinaryWriter(buffer);

        while (true) {
            char ch = readChar();

            if (ch == 0) {
                break;
            }

            bw.writeChar(ch);
        }

        byte[] bytes = buffer.toByteArray();
        position += bytes.length;
        return new String(bytes, charset);
    }

    public int getPosition() {
        return position;
    }
}