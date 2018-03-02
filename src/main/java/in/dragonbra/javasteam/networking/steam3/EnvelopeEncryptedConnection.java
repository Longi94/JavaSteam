package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.base.Msg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.generated.MsgChannelEncryptRequest;
import in.dragonbra.javasteam.generated.MsgChannelEncryptResponse;
import in.dragonbra.javasteam.generated.MsgChannelEncryptResult;
import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.util.KeyDictionary;
import in.dragonbra.javasteam.util.crypto.CryptoHelper;
import in.dragonbra.javasteam.util.crypto.RSACrypto;
import in.dragonbra.javasteam.util.event.EventArgs;
import in.dragonbra.javasteam.util.event.EventHandler;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public class EnvelopeEncryptedConnection extends Connection {

    private static final Logger logger = LogManager.getLogger(EnvelopeEncryptedConnection.class);

    private final Connection inner;
    private final EUniverse universe;
    private EncryptionState state;
    private INetFilterEncryption encryption;

    private final EventHandler<EventArgs> onConnected = new EventHandler<EventArgs>() {
        @Override
        public void handleEvent(Object sender, EventArgs e) {
            state = EncryptionState.CONNECTED;
        }
    };

    private final EventHandler<DisconnectedEventArgs> onDisconnected = new EventHandler<DisconnectedEventArgs>() {
        @Override
        public void handleEvent(Object sender, DisconnectedEventArgs e) {
            state = EncryptionState.DISCONNECTED;
            encryption = null;

            disconnected.handleEvent(EnvelopeEncryptedConnection.this, e);
        }
    };

    private final EventHandler<NetMsgEventArgs> onNetMsgReceived = new EventHandler<NetMsgEventArgs>() {
        @Override
        public void handleEvent(Object sender, NetMsgEventArgs e) {
            if (state == EncryptionState.ENCRYPTED) {
                byte[] plaintextData = encryption.processIncoming(e.getData());
                netMsgReceived.handleEvent(EnvelopeEncryptedConnection.this, e.withData(plaintextData));
                return;
            }

            IPacketMsg packetMsg = CMClient.getPacketMsg(e.getData());

            if (!isExpectedEMsg(packetMsg.getMsgType())) {
                logger.debug("Rejected EMsg: " + packetMsg.getMsgType() + " during channel setup");
                return;
            }

            switch (packetMsg.getMsgType()) {
                case ChannelEncryptRequest:
                    handleEncryptRequest(packetMsg);
                    break;
                case ChannelEncryptResult:
                    handleEncryptResult(packetMsg);
                    break;
            }
        }
    };

    public EnvelopeEncryptedConnection(Connection inner, EUniverse universe) {
        if (inner == null) {
            throw new IllegalArgumentException("inner connection is null");
        }
        this.inner = inner;
        this.universe = universe;

        inner.getNetMsgReceived().addEventHandler(onNetMsgReceived);
        inner.getConnected().addEventHandler(onConnected);
        inner.getDisconnected().addEventHandler(onDisconnected);
    }

    private void handleEncryptRequest(IPacketMsg packetMsg) {
        Msg<MsgChannelEncryptRequest> request = new Msg<>(MsgChannelEncryptRequest.class, packetMsg);

        EUniverse connectedUniverse = request.getBody().getUniverse();
        long protoVersion = request.getBody().getProtocolVersion();

        logger.debug("Got encryption request. Universe: " + connectedUniverse + " Protocol ver: " + protoVersion);

        if (protoVersion != MsgChannelEncryptRequest.PROTOCOL_VERSION) {
            logger.debug("Encryption handshake protocol version mismatch!");
        }

        if (connectedUniverse != universe) {
            logger.debug("Expected universe " + universe + " but server reported universe " + connectedUniverse);
        }

        byte[] randomChallenge = null;
        if (request.getPayload().getLength() >= 16) {
            randomChallenge = request.getPayload().toByteArray();
        }

        byte[] publicKey = KeyDictionary.getPublicKey(connectedUniverse);

        if (publicKey == null) {
            logger.debug("HandleEncryptRequest got request for invalid universe! Universe: " + connectedUniverse + " Protocol ver: " + protoVersion);
            disconnect();
        }

        Msg<MsgChannelEncryptResponse> response = new Msg<>(MsgChannelEncryptResponse.class);

        byte[] tempSessionKey = CryptoHelper.generateRandomBlock(32);
        byte[] encryptedHandshakeBlob = null;

        RSACrypto rsa = new RSACrypto(publicKey);

        if (randomChallenge != null) {
            byte[] blobToEncrypt = new byte[tempSessionKey.length + randomChallenge.length];

            System.arraycopy(tempSessionKey, 0, blobToEncrypt, 0, tempSessionKey.length);
            System.arraycopy(randomChallenge, 0, blobToEncrypt, tempSessionKey.length, randomChallenge.length);

            encryptedHandshakeBlob = rsa.encrypt(blobToEncrypt);
        } else {
            encryptedHandshakeBlob = rsa.encrypt(tempSessionKey);
        }

        byte[] keyCrc = CryptoHelper.crcHash(encryptedHandshakeBlob);

        try {
            response.write(encryptedHandshakeBlob);
            response.write(keyCrc);
            response.write(0);
        } catch (IOException e) {
            logger.debug(e);
        }

        if (randomChallenge != null) {
            encryption = new NetFilterEncryptionWithHMAC(tempSessionKey);
        } else {
            encryption = new NetFilterEncryption(tempSessionKey);
        }

        state = EncryptionState.CHALLENGED;

        send(response.serialize());
    }

    private void handleEncryptResult(IPacketMsg packetMsg) {
        Msg<MsgChannelEncryptResult> result = new Msg<>(MsgChannelEncryptResult.class, packetMsg);

        logger.debug("Encryption result: " + result.getBody().getResult());

        assert encryption != null;

        if (result.getBody().getResult() == EResult.OK && encryption != null) {
            state = EncryptionState.ENCRYPTED;
            connected.handleEvent(this, EventArgs.EMPTY);
        } else {
            logger.debug("Encryption channel setup failed");
            disconnect();
        }
    }

    private boolean isExpectedEMsg(EMsg msg) {
        switch (state) {
            case DISCONNECTED:
                return false;
            case CONNECTED:
                return msg == EMsg.ChannelEncryptRequest;
            case CHALLENGED:
                return msg == EMsg.ChannelEncryptResult;
            case ENCRYPTED:
                return true;
            default:
                throw new IllegalStateException("Unreachable - landed up in undefined state.");
        }
    }

    @Override
    public void connect(InetSocketAddress endPoint) {
        inner.connect(endPoint);
    }

    @Override
    public void disconnect() {
        inner.disconnect();
    }

    @Override
    public void send(byte[] data) {
        if (state == EncryptionState.ENCRYPTED) {
            data = encryption.processOutgoing(data);
        }

        inner.send(data);
    }

    @Override
    public InetAddress getLocalIP() {
        return inner.getLocalIP();
    }

    @Override
    public InetSocketAddress getCurrentEndPoint() {
        return inner.getCurrentEndPoint();
    }

    @Override
    public ProtocolTypes getProtocolTypes() {
        return inner.getProtocolTypes();
    }

    private enum EncryptionState {
        DISCONNECTED,
        CONNECTED,
        CHALLENGED,
        ENCRYPTED
    }
}
