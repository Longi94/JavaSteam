package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class WebSocketConnection extends Connection implements WebSocketCMClient.WSListener {

    private static final Logger logger = LogManager.getLogger(WebSocketConnection.class);

    private final AtomicReference<WebSocketCMClient> client = new AtomicReference<>(null);

    private volatile boolean userInitiated = false;

    private InetSocketAddress socketEndPoint;

    @Override
    public void connect(InetSocketAddress endPoint, int timeout) {
        logger.debug("Connecting to " + endPoint + "...");
        WebSocketCMClient newClient = new WebSocketCMClient(getUri(endPoint), timeout, this);
        WebSocketCMClient oldClient = client.getAndSet(newClient);
        if (oldClient != null) {
            logger.debug("Attempted to connect while already connected. Closing old connection...");
            oldClient.close();
        }

        socketEndPoint = endPoint;

        newClient.connect();
    }

    @Override
    public void disconnect() {
        disconnectCore(true);
    }

    @Override
    public void send(byte[] data) {
        try {
            if (client.get() == null) {
                // If we're in the process of being disconnected using WebSocket,
                // and our client is still sending data to steam during that process.
                // Our `client` reference is most likely null and the exception doesn't handle it right.
                logger.debug("WebSocket client is null");
                return;
            }

            client.get().send(data);
        } catch (Exception e) {
            logger.debug("Exception while sending data", e);
            disconnectCore(false);
        }
    }

    @Override
    public InetAddress getLocalIP() {
        return client.get().getLocalSocketAddress().getAddress();
    }

    @Override
    public InetSocketAddress getCurrentEndPoint() {
        return socketEndPoint;
    }

    @Override
    public ProtocolTypes getProtocolTypes() {
        return ProtocolTypes.WEB_SOCKET;
    }

    private void disconnectCore(boolean userInitiated) {
        WebSocketCMClient oldClient = client.getAndSet(null);

        if (oldClient != null) {
            oldClient.close();
            this.userInitiated = userInitiated;
        }

        socketEndPoint = null;
    }

    private static URI getUri(InetSocketAddress address) {
        return URI.create("wss://" + address.getHostString() + ":" + address.getPort() + "/cmsocket/");
    }

    @Override
    public void onData(byte[] data) {
        if (data != null && data.length > 0) {
            onNetMsgReceived(new NetMsgEventArgs(data, getCurrentEndPoint()));
        }
    }

    @Override
    public void onClose(boolean remote) {
        onDisconnected(userInitiated && !remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.debug("error in websocket", ex);
    }

    @Override
    public void onOpen() {
        logger.debug("Connected to " + getCurrentEndPoint());
        onConnected();
    }
}
