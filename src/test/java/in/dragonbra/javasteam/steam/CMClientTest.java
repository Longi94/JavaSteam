package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.base.ISteamSerializableHeader;
import in.dragonbra.javasteam.base.PacketClientMsgProtobuf;
import in.dragonbra.javasteam.base.PacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgHdr;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;
import in.dragonbra.javasteam.util.stream.BinaryWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class CMClientTest extends TestBase {
    @Test
    public void getPacketMsgReturnsPacketMsgForCryptoHandshake() throws IOException {
        EMsg[] messages = new EMsg[]{
                EMsg.ChannelEncryptRequest,
                EMsg.ChannelEncryptResponse,
                EMsg.ChannelEncryptResult
        };

        for (EMsg message : messages) {
            MsgHdr msgHdr = new MsgHdr();
            msgHdr.setEMsg(message);

            byte[] data = serialize(msgHdr);

            IPacketMsg packetMsg = CMClient.getPacketMsg(data);
            assertInstanceOf(PacketMsg.class, packetMsg);
        }
    }

    @Test
    public void getPacketMsgReturnsPacketClientMsgProtobufForMessagesWithProtomask() throws IOException {
        MsgHdrProtoBuf msgHdr = new MsgHdrProtoBuf();
        msgHdr.setMsg(EMsg.ClientLogOnResponse);

        byte[] data = serialize(msgHdr);
        IPacketMsg packetMsg = CMClient.getPacketMsg(data);
        assertInstanceOf(PacketClientMsgProtobuf.class, packetMsg);
    }

    @Test
    public void getPacketMsgFailsWithNull() throws IOException {
        MsgHdrProtoBuf msgHdr = new MsgHdrProtoBuf();
        msgHdr.setMsg(EMsg.ClientLogOnResponse);

        byte[] data = serialize(msgHdr);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryWriter bw = new BinaryWriter(baos);
        bw.writeInt(-1);
        System.arraycopy(baos.toByteArray(), 0, data, 4, 4);
        IPacketMsg packetMsg = CMClient.getPacketMsg(data);
        assertNull(packetMsg);
    }

    @Test
    public void getPacketMsgFailsWithTinyArray() {
        byte[] data = new byte[3];
        IPacketMsg packetMsg = CMClient.getPacketMsg(data);
        assertNull(packetMsg);
    }

    private static byte[] serialize(ISteamSerializableHeader hdr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hdr.serialize(baos);
        return baos.toByteArray();
    }
}
