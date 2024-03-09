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
    final Event<NetMsgEventArgs> netMsgReceived = new Event<>();

    /**
     * Occurs when the physical connection is established.
     */
    final Event<EventArgs> connected = new Event<>();

    /**
     * Occurs when the physical connection is broken.
     */
    final Event<DisconnectedEventArgs> disconnected = new Event<>();

    void onNetMsgReceived(NetMsgEventArgs e) {
        netMsgReceived.handleEvent(this, e);
    }

    void onConnected() {
        connected.handleEvent(this, null);
    }

    void onDisconnected(boolean e) {
        disconnected.handleEvent(this, new DisconnectedEventArgs(e));
    }

    /**
     * Connects to the specified end point.
     *
     * @param endPoint The end point to connect to.
     * @param timeout  Timeout in milliseconds
     */
    public abstract void connect(InetSocketAddress endPoint, int timeout);

    /**
     * Connects to the specified end point.
     *
     * @param endPoint The end point to connect to.
     */
    public final void connect(InetSocketAddress endPoint) {
        connect(endPoint, 5000);
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

    public abstract InetSocketAddress getCurrentEndPoint();

    /**
     * @return The type of communication protocol that this connection uses.
     */
    public abstract ProtocolTypes getProtocolTypes();

    public Event<NetMsgEventArgs> getNetMsgReceived() {
        return netMsgReceived;
    }

    public Event<EventArgs> getConnected() {
        return connected;
    }

    public Event<DisconnectedEventArgs> getDisconnected() {
        return disconnected;
    }
}
