package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.base.IClientMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EServerType;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.Connection;
import in.dragonbra.javasteam.networking.steam3.NetMsgEventArgs;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.networking.steam3.TcpConnection;
import in.dragonbra.javasteam.steam.discovery.ServerQuality;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.discovery.SmartCMServerList;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.IDebugNetworkListener;
import in.dragonbra.javasteam.util.event.EventArgs;
import in.dragonbra.javasteam.util.event.ScheduledFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * This base client handles the underlying connection to a CM server. This class should not be use directly, but through
 * the {@link SteamClient} class.
 */
public abstract class CMClient {

    private static final Logger logger = LogManager.getLogger(CMClient.class);

    private SteamConfiguration configuration;

    private boolean connected;

    private long sessionToken;

    private Integer cellID;

    private Integer sessionID;

    private SteamID steamID;

    private IDebugNetworkListener debugNetworkListener;

    private boolean expectDisconnection;

    // connection lock around the setup and tear down of the connection task
    private final Object connectionLock = new Object();

    private Object connectionSetupTask;

    private Connection connection;

    private ScheduledFunction heartBeatFunc;

    private Map<EServerType, Set<InetSocketAddress>> serverMap;

    public CMClient(SteamConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration is null");
        }

        this.configuration = configuration;
        this.serverMap = new HashMap<>();

        heartBeatFunc = new ScheduledFunction(() -> {
            // TODO: 2018-02-21
        }, 5000);
    }

    /**
     * Connects this client to a Steam3 server. This begins the process of connecting and encrypting the data channel
     * between the client and the server. Results are returned asynchronously in a {@link ConnectedCallback}. If the
     * server that SteamKit attempts to connect to is down, a {@link DisconnectedCallback} will be posted instead.
     * SteamKit will not attempt to reconnect to Steam, you must handle this callback and call Connect again preferably
     * after a short delay. SteamKit will randomly select a CM server from its internal list.
     */
    public void connect() {
        connect(null);
    }

    /**
     * Connects this client to a Steam3 server. This begins the process of connecting and encrypting the data channel
     * between the client and the server. Results are returned asynchronously in a {@link ConnectedCallback}. If the
     * server that SteamKit attempts to connect to is down, a {@link DisconnectedCallback} will be posted instead.
     * SteamKit will not attempt to reconnect to Steam, you must handle this callback and call Connect again preferably
     * after a short delay.
     *
     * @param cmServer The {@link ServerRecord} of the CM server to connect to.
     */
    public void connect(ServerRecord cmServer) {
        synchronized (connectionLock) {
            disconnect();

            assert connection != null;

            expectDisconnection = false;

            if (cmServer == null) {
                cmServer = getServers().getNextServerCandidate(configuration.getProtocolTypes());
            }


        }
    }

    /**
     * Disconnects this client.
     */
    public void disconnect() {
        synchronized (connectionLock) {
            heartBeatFunc.stop();

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Sends the specified client message to the server. This method automatically assigns the correct SessionID and
     * SteamID of the message.
     *
     * @param msg The client message to send.
     */
    public void send(IClientMsg msg) {
        // TODO: 2018-02-21  
    }

    /**
     * Returns the list of servers matching the given type
     *
     * @param type Server type requested
     * @return List of server endpoints
     */
    public List<InetSocketAddress> getServers(EServerType type) {
        if (serverMap.containsKey(type)) {
            return new ArrayList<>(serverMap.get(type));
        }

        return new ArrayList<>();
    }

    protected boolean onClientMsgReceived(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            logger.debug("Packet message failed to parse, shutting down connection");
            disconnect();
            return false;
        }

        logger.debug(String.format("<- Recv'd EMsg: %s (%d) (Proto: %s)", packetMsg.getMsgType(), packetMsg.getMsgType().code(), packetMsg.isProto()));

        // Multi message gets logged down the line after it's decompressed
        if (packetMsg.getMsgType() != EMsg.Multi) {
            try {
                if (debugNetworkListener != null) {
                    debugNetworkListener.onIncomingNetworkMessage(packetMsg.getMsgType(), packetMsg.getData());
                }
            } catch (Exception e) {
                logger.debug("debugNetworkListener threw an exception", e);
            }
        }

        switch (packetMsg.getMsgType()) {
            case Multi:
                // TODO: 2018-02-21
                break;
            case ClientLogOnResponse: // we handle this to get the SteamID/SessionID and to setup heartbeating
                // TODO: 2018-02-21
                break;
            case ClientLoggedOff: // to stop heartbeating when we get logged off
                // TODO: 2018-02-21
                break;
            case ClientServerList: // Steam server list
                // TODO: 2018-02-21
                break;
            case ClientCMList:
                // TODO: 2018-02-21
                break;
            case ClientSessionToken: // am session token
                // TODO: 2018-02-21
                break;
        }

        return true;
    }

    /**
     * Called when the client is securely connected to Steam3.
     */
    protected void onClientDConnected() {

    }

    /**
     * Called when the client is physically disconnected from Steam3.
     */
    protected void onClientDisconnected(boolean userInitiated) {
        serverMap.values().forEach(Set::clear);
    }

    private Connection createConnection(ProtocolTypes protocol) {
        if ((protocol.code() & ProtocolTypes.WEB_SOCKET.code()) > 0) {
            // TODO: 2018-02-21  
            //return new WebSocketConnection();
        } else if ((protocol.code() & ProtocolTypes.TCP.code()) > 0) {
            return new TcpConnection();
        } else if ((protocol.code() & ProtocolTypes.UDP.code()) > 0) {
            // TODO: 2018-02-21  
            //return new EnvelopeEncryptedConnection(new UdpConnection(), getUniverse());
        }

        throw new IllegalArgumentException("Protocol bitmask has no supported protocols set.");
    }

    private void netMsgReceived(Object sender, NetMsgEventArgs e) {
        onClientMsgReceived(getPacketMsg(e.getData()));
    }

    private void connected(Object sender, EventArgs e) {
        getServers().tryMark(connection.getCurrentEndPoint(), connection.getProtocolTypes(), ServerQuality.GOOD);

        connected = true;
        onClientDConnected();
    }

    public SteamConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return Bootstrap list of CM servers.
     */
    public SmartCMServerList getServers() {
        return configuration.getServerList();
    }

    /**
     * Returns the the local IP of this client.
     *
     * @return The local IP.
     */
    public InetAddress getLocalIP() {
        return connection.getLocalIP();
    }

    /**
     * Gets the universe of this client.
     *
     * @return The universe.
     */
    public EUniverse getUniverse() {
        return configuration.getUniverse();
    }

    /**
     * Gets a value indicating whether this instance is connected to the remote CM server.
     *
     * @return <c>true</c> if this instance is connected; otherwise, <c>false</c>.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Gets the session token assigned to this client from the AM.
     */
    public long getSessionToken() {
        return sessionToken;
    }

    /**
     * Gets the Steam recommended Cell ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     */
    public Integer getCellID() {
        return cellID;
    }

    /**
     * Gets the session ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     *
     * @return The session ID.
     */
    public Integer getSessionID() {
        return sessionID;
    }

    /**
     * Gets the SteamID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     *
     * @return The SteamID.
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * Gets or sets the connection timeout used when connecting to the Steam server.
     *
     * @return The connection timeout.
     */
    public long getConnectionTimeout() {
        return configuration.getConnectionTimeout();
    }

    /**
     * Gets the network listening interface. Use this for debugging only.
     * For your convenience, you can use {@link NetHookNetworkListener} class.
     */
    public IDebugNetworkListener getDebugNetworkListener() {
        return debugNetworkListener;
    }

    /**
     * Sets the network listening interface. Use this for debugging only.
     * For your convenience, you can use {@link NetHookNetworkListener} class.
     */
    public void setDebugNetworkListener(IDebugNetworkListener debugNetworkListener) {
        this.debugNetworkListener = debugNetworkListener;
    }

    boolean isExpectDisconnection() {
        return expectDisconnection;
    }

    void setExpectDisconnection(boolean expectDisconnection) {
        this.expectDisconnection = expectDisconnection;
    }
}
