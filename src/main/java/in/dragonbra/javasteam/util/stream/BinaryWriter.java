package in.dragonbra.javasteam.util.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Basically DataOutputStream, but the bytes are parsed in reverse order
 */
public class BinaryWriter extends FilterOutputStream {

    private final byte[] writeBuffer = new byte[8];

    public BinaryWriter(OutputStream out) {
        super(out);
    }

    public void writeInt(int v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 24) & 0xFF);
    }

    public void writeShort(short v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
    }

    public void writeLong(long v) throws IOException {
        writeBuffer[7] = (byte) (v >>> 56);
        writeBuffer[6] = (byte) (v >>> 48);
        writeBuffer[5] = (byte) (v >>> 40);
        writeBuffer[4] = (byte) (v >>> 32);
        writeBuffer[3] = (byte) (v >>> 24);
        writeBuffer[2] = (byte) (v >>> 16);
        writeBuffer[1] = (byte) (v >>> 8);
        writeBuffer[0] = (byte) v;
        out.write(writeBuffer, 0, 8);
    }

    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeBoolean(boolean v) throws IOException {
        out.write(v ? 1 : 0);
    }

    public void writeByte(byte v) throws IOException {
        out.write(v);
    }

    public void writeChar(char v) throws IOException {
        out.write(v);
    }
}
