package in.dragonbra.javasteam.util.stream;

import in.dragonbra.javasteam.util.compat.ByteArrayOutputStreamCompat;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
        int n = in.read(readBuffer, 0, 4);
        if (n < 4) {
            throw new EOFException();
        }
        position += 4;

        return ((readBuffer[3] & 0xFF) << 24) |
                ((readBuffer[2] & 0xFF) << 16) |
                ((readBuffer[1] & 0xFF) << 8) |
                (readBuffer[0] & 0xFF);
    }

    public byte[] readBytes(int len) throws IOException {
        if (len < 0) {
            throw new IOException("negative length");
        }

        if (len == 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[len];
        int totalRead = 0;

        while (totalRead < len) {
            int read = in.read(bytes, totalRead, len - totalRead);
            if (read < 0) {
                throw new EOFException();
            }
            totalRead += read;
        }

        position += totalRead;
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
        int n = in.read(readBuffer, 0, 2);
        if (n < 2) {
            throw new EOFException();
        }
        position += 2;

        return (short) (((readBuffer[1] & 0xFF) << 8) | (readBuffer[0] & 0xFF));
    }

    public long readLong() throws IOException {
        int n = in.read(readBuffer, 0, 8);
        if (n < 8) {
            throw new EOFException();
        }
        position += 8;

        return ((long) (readBuffer[7] & 0xFF) << 56) |
                ((long) (readBuffer[6] & 0xFF) << 48) |
                ((long) (readBuffer[5] & 0xFF) << 40) |
                ((long) (readBuffer[4] & 0xFF) << 32) |
                ((long) (readBuffer[3] & 0xFF) << 24) |
                ((long) (readBuffer[2] & 0xFF) << 16) |
                ((long) (readBuffer[1] & 0xFF) << 8) |
                (long) (readBuffer[0] & 0xFF);
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
        return readNullTermString(StandardCharsets.UTF_8);
    }

    public String readNullTermString(Charset charset) throws IOException {
        if (charset == null) {
            throw new IOException("charset is null");
        }

        if (charset.equals(StandardCharsets.UTF_8)) {
            return readNullTermUtf8String();
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(64);

        while (true) {
            int ch = in.read();
            if (ch < 0) {
                throw new EOFException();
            }

            position++;

            if (ch == 0) {
                break;
            }

            buffer.write(ch);
        }

        return ByteArrayOutputStreamCompat.toString(buffer, charset);
    }

    private String readNullTermUtf8String() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;

        while ((b = in.read()) != 0) {
            if (b <= 0) {
                break;
            }
            baos.write(b);
            position++;
        }

        position++; // Increment for the null terminator

        return ByteArrayOutputStreamCompat.toString(baos);
    }

    public int getPosition() {
        return position;
    }
}
