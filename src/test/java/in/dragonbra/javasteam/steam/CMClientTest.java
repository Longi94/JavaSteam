package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.base.*;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EServerType;
import in.dragonbra.javasteam.generated.MsgHdr;
import in.dragonbra.javasteam.generated.MsgHdrProtoBuf;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientServerList;
import in.dragonbra.javasteam.util.stream.BinaryWriter;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

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
            assertTrue(packetMsg instanceof PacketMsg);
        }
    }

    @Test
    public void getPacketMsgReturnsPacketClientMsgProtobufForMessagesWithProtomask() throws IOException {
        MsgHdrProtoBuf msgHdr = new MsgHdrProtoBuf();
        msgHdr.setMsg(EMsg.ClientLogOnResponse);

        byte[] data = serialize(msgHdr);
        IPacketMsg packetMsg = CMClient.getPacketMsg(data);
        assertTrue(packetMsg instanceof PacketClientMsgProtobuf);
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

    @Test
    public void serverLookupIsClearedWhenDisconnecting() {
        ClientMsgProtobuf<CMsgClientServerList.Builder> msg = new ClientMsgProtobuf<CMsgClientServerList.Builder>(CMsgClientServerList.class, EMsg.ClientServerList);
        CMsgClientServerList.Server server = CMsgClientServerList.Server.newBuilder()
                .setServerIp(0x7F000001)
                .setServerType(EServerType.CM.code())
                .setServerPort(1234)
                .build();
        msg.getBody().addServers(server);

        DummyClient client = new DummyClient();

        client.handleClientMsg(msg);
        assertEquals(1, client.getServers(EServerType.CM).size());

        client.dummyDisconnect();
        assertEquals(0, client.getServers(EServerType.CM).size());
    }

    @Test
    public void serverLookupDoesNotAccumulateDuplicates() {
        ClientMsgProtobuf<CMsgClientServerList.Builder> msg = new ClientMsgProtobuf<CMsgClientServerList.Builder>(CMsgClientServerList.class, EMsg.ClientServerList);
        CMsgClientServerList.Server server = CMsgClientServerList.Server.newBuilder()
                .setServerIp(0x7F000001)
                .setServerType(EServerType.CM.code())
                .setServerPort(1234)
                .build();
        msg.getBody().addServers(server);

        DummyClient client = new DummyClient();

        client.handleClientMsg(msg);
        assertEquals(1, client.getServers(EServerType.CM).size());

        client.handleClientMsg(msg);
        assertEquals(1, client.getServers(EServerType.CM).size());
    }

    private static byte[] serialize(ISteamSerializableHeader hdr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hdr.serialize(baos);
        return baos.toByteArray();
    }
}