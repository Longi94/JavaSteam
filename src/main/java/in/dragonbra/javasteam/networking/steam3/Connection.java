package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.event.Event;
import in.dragonbra.javasteam.util.event.EventArgs;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public abstract class Connection {

    /**
     * Occurs when a net message is received over the network.
     */
    private Event<NetMsgEventArgs> netMsgReceived = new Event<>();

    /**
     * Occurs when the physical connection is established.
     */
    private Event<EventArgs> connected = new Event<>();

    /**
     * Occurs when the physical connection is broken.
     */
    private Event<DisconnectedEventArgs> disconnected = new Event<>();

    private InetSocketAddress currentEndPoint;

    private ProtocolTypes protocolTypes;

    protected void onNetMsgReceived(NetMsgEventArgs e) {
        if (netMsgReceived != null) {
            netMsgReceived.handleEvent(this, e);
        }
    }

    protected void onConnected() {
        if (connected != null) {
            connected.handleEvent(this, null);
        }
    }

    protected void onDisconnected(boolean e) {
        if (disconnected != null) {
            disconnected.handleEvent(this, new DisconnectedEventArgs(e));
        }
    }

    /**
     * Connects to the specified end point.
     *
     * @param endPoint The end point to connect to.
     * @param timeout  Timeout in milliseconds
     */
    public abstract void connect(InetSocketAddress endPoint, long timeout);

    /**
     * Connects to the specified end point.
     *
     * @param endPoint The end point to connect to.
     */
    public void connect(InetSocketAddress endPoint) {
        this.connect(endPoint, 5000L);
    }

    /**
     * Disconnects this instance.
     */
    public abstract void disconnect();

    /**
     * Sends the specified data packet.
     *
     * @param data The data packet to send.
     */
    public abstract void send(byte[] data);

    /**
     * Gets the local IP.
     *
     * @return The local IP.
     */
    public abstract InetAddress getLocalIP();

    public InetSocketAddress getCurrentEndPoint() {
        return currentEndPoint;
    }

    /**
     * @return The type of communication protocol that this connection uses.
     */
    public ProtocolTypes getProtocolTypes() {
        return protocolTypes;
    }
}
