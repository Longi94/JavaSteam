package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.enums.EUdpPacketType;
import in.dragonbra.javasteam.generated.UdpHeader;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author lngtr
 * @since 2018-03-01
 */
class UdpPacket {
    public static final int MAX_PAYLOAD = 0x4DC;

    private UdpHeader header;

    private MemoryStream payload;

    /**
     * Initializes a new instance of the {@link UdpPacket} class with
     * Header is populated from the MemoryStream
     *
     * @param ms The stream containing the packet and it's payload data.
     */
    UdpPacket(MemoryStream ms) {
        header = new UdpHeader();

        try {
            header.deserialize(ms);
        } catch (IOException e) {
            return;
        }

        if (header.getMagic() != UdpHeader.MAGIC) {
            return;
        }

        setPayload(ms, header.getPayloadSize());
    }

    /**
     * Initializes a new instance of the {@link UdpPacket} class, with no payload.
     * Header must be populated manually.
     *
     * @param type The type.
     */
    UdpPacket(EUdpPacketType type) {
        header = new UdpHeader();
        payload = new MemoryStream();

        header.setPacketType(type);
    }

    /**
     * Initializes a new instance of the {@link UdpPacket} class, of the specified type containing the specified payload.
     * Header must be populated manually.
     *
     * @param type    The type.
     * @param payload The payload.
     */
    UdpPacket(EUdpPacketType type, MemoryStream payload) {
        this(type);
        setPayload(payload);
    }

    /**
     * Initializes a new instance of the {@link UdpPacket} class, of the specified type containing the first 'length' bytes of specified payload.
     * Header must be populated manually.
     *
     * @param type    The type.
     * @param payload The payload.
     * @param length  The length.
     */
    UdpPacket(EUdpPacketType type, MemoryStream payload, long length) {
        this(type);
        setPayload(payload, length);
    }

    /**
     * Sets the payload
     *
     * @param ms The payload to copy.
     */
    public void setPayload(MemoryStream ms) {
        setPayload(ms, ms.getLength() - ms.getPosition());
    }

    public void setPayload(MemoryStream ms, long length) {
        if (length > MAX_PAYLOAD) {
            throw new IllegalArgumentException("Payload length exceeds 0x4DC maximum");
        }

        byte[] buf = new byte[(int) length];
        ms.read(buf, 0, buf.length);

        payload = new MemoryStream(buf);
        header.setPayloadSize((short) payload.getLength());
        header.setMsgSize((int) payload.getLength());

    }

    /**
     * Serializes the UdpPacket.
     *
     * @return The serialized packet.
     */
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            header.serialize(baos);
            payload.seek(0, SeekOrigin.BEGIN);
            baos.write(payload.toByteArray());
        } catch (IOException ignored) {
        }

        return baos.toByteArray();
    }

    /**
     * Gets a value indicating whether this instance is valid.
     *
     * @return <b>true</b> if this instance is valid; otherwise, <b>false</b>.
     */
    public boolean isValid() {
        return header.getMagic() == UdpHeader.MAGIC &&
                header.getPayloadSize() <= MAX_PAYLOAD &&
                payload != null;
    }

    public UdpHeader getHeader() {
        return header;
    }

    public MemoryStream getPayload() {
        return payload;
    }
}
