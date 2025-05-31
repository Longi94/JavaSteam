package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.base.*;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.generated.MsgClientLogon;
import in.dragonbra.javasteam.generated.MsgClientServerUnavailable;
import in.dragonbra.javasteam.networking.steam3.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgMulti;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientSessionToken;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientHeartBeat;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientHello;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLoggedOff;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse;
import in.dragonbra.javasteam.steam.discovery.ServerQuality;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.discovery.SmartCMServerList;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.*;
import in.dragonbra.javasteam.util.event.EventArgs;
import in.dragonbra.javasteam.util.event.EventHandler;
import in.dragonbra.javasteam.util.event.ScheduledFunction;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.zip.GZIPInputStream;

/**
 * This base client handles the underlying connection to a CM server. This class should not be use directly, but through
 * the {@link in.dragonbra.javasteam.steam.steamclient.SteamClient SteamClient} class.
 */
public abstract class CMClient {

    private static final Logger logger = LogManager.getLogger(CMClient.class);

    private final SteamConfiguration configuration;

    @Nullable
    private InetAddress publicIP;

    @Nullable
    private String ipCountryCode;

    private boolean isConnected;

    private long sessionToken;

    @Nullable
    private Integer cellID;

    @Nullable
    private Integer sessionID;

    @Nullable
    private SteamID steamID;

    private IDebugNetworkListener debugNetworkListener;

    private boolean expectDisconnection;

    // connection lock around the setup and tear down of the connection task
    private final Object connectionLock = new Object();

    @Nullable
    private Connection connection;

    private final ScheduledFunction heartBeatFunc;

    private final EventHandler<NetMsgEventArgs> netMsgReceived = (sender, e) -> onClientMsgReceived(getPacketMsg(e.getData()));

    private final EventHandler<EventArgs> connected = (sender, e) -> {
        logger.debug("EventHandler `connected` called");

        getServers().tryMark(connection.getCurrentEndPoint(), connection.getProtocolTypes(), ServerQuality.GOOD);

        isConnected = true;

        try {
            onClientConnected();
        } catch (Exception ex) {
            logger.error("Unhandled exception after connecting: ", ex);
            disconnect(false);
        }
    };

    private final EventHandler<DisconnectedEventArgs> disconnected = new EventHandler<>() {
        @Override
        public void handleEvent(Object sender, DisconnectedEventArgs e) {
            logger.debug("EventHandler `disconnected` called. User Initiated: " + e.isUserInitiated() +
                    ", Expected Disconnection: " + expectDisconnection);

            isConnected = false;

            if (!e.isUserInitiated() && !expectDisconnection) {
                getServers().tryMark(connection.getCurrentEndPoint(), connection.getProtocolTypes(), ServerQuality.BAD);
            }

            sessionID = null;
            steamID = null;

            connection.getNetMsgReceived().removeEventHandler(netMsgReceived);
            connection.getConnected().removeEventHandler(connected);
            connection.getDisconnected().removeEventHandler(this);
            connection = null;

            heartBeatFunc.stop();

            onClientDisconnected(e.isUserInitiated() || expectDisconnection);
        }
    };

    public CMClient(SteamConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration is null");
        }

        this.configuration = configuration;

        heartBeatFunc = new ScheduledFunction(() -> {
            var heartbeat = new ClientMsgProtobuf<CMsgClientHeartBeat.Builder>(
                    CMsgClientHeartBeat.class, EMsg.ClientHeartBeat);
            heartbeat.getBody().setSendReply(true); // Ping Pong
            send(heartbeat);
        }, 5000);
    }

    /**
     * Debugging only method:
     * Do not use this directly.
     */
    public void receiveTestPacketMsg(IPacketMsg packetMsg) {
        onClientMsgReceived(packetMsg);
    }

    /**
     * Debugging only method:
     * Do not use this directly.
     */
    public void setIsConnected(boolean value) {
        isConnected = value;
    }

    /**
     * Connects this client to a Steam3 server. This begins the process of connecting and encrypting the data channel
     * between the client and the server. Results are returned asynchronously in a {@link in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback ConnectedCallback}. If the
     * server that SteamKit attempts to connect to is down, a {@link in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback DisconnectedCallback} will be posted instead.
     * SteamKit will not attempt to reconnect to Steam, you must handle this callback and call Connect again preferably
     * after a short delay. SteamKit will randomly select a CM server from its internal list.
     */
    public void connect() {
        connect(null);
    }

    /**
     * Connects this client to a Steam3 server. This begins the process of connecting and encrypting the data channel
     * between the client and the server. Results are returned asynchronously in a {@link in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback ConnectedCallback}. If the
     * server that SteamKit attempts to connect to is down, a {@link in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback DisconnectedCallback} will be posted instead.
     * SteamKit will not attempt to reconnect to Steam, you must handle this callback and call Connect again preferably
     * after a short delay.
     *
     * @param cmServer The {@link ServerRecord} of the CM server to connect to.
     */
    public void connect(ServerRecord cmServer) {
        synchronized (connectionLock) {
            try {
                disconnect(true);

                assert connection == null;

                expectDisconnection = false;

                if (cmServer == null) {
                    cmServer = getServers().getNextServerCandidate(configuration.getProtocolTypes());
                }

                if (cmServer == null) {
                    logger.error("No CM servers available to connect to");
                    onClientDisconnected(false);
                    return;
                }

                connection = createConnection(cmServer.getProtocolTypes());
                connection.getNetMsgReceived().addEventHandler(netMsgReceived);
                connection.getConnected().addEventHandler(connected);
                connection.getDisconnected().addEventHandler(disconnected);
                logger.debug(String.format("Connecting to %s with protocol %s, and with connection impl %s",
                        cmServer.getEndpoint(), cmServer.getProtocolTypes(), connection.getClass().getSimpleName()));
                connection.connect(cmServer.getEndpoint());
            } catch (Exception e) {
                logger.debug("Failed to connect to Steam network", e);
                onClientDisconnected(false);
            }
        }
    }

    /**
     * Disconnects this client.
     */
    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(boolean userInitiated) {
        synchronized (connectionLock) {
            heartBeatFunc.stop();

            if (connection != null) {
                connection.disconnect(userInitiated);
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
        if (msg == null) {
            throw new IllegalArgumentException("A value for 'msg' must be supplied");
        }

        Integer _sessionID = this.sessionID;

        if (_sessionID != null) {
            msg.setSessionID(_sessionID);
        }

        SteamID _steamID = this.steamID;

        if (_steamID != null) {
            msg.setSteamID(_steamID);
        }

        try {
            if (debugNetworkListener != null) {
                debugNetworkListener.onOutgoingNetworkMessage(msg.getMsgType(), msg.serialize());
            }
        } catch (Exception e) {
            logger.debug("DebugNetworkListener threw an exception", e);
        }

        // we'll swallow any network failures here because they will be thrown later
        // on the network thread, and that will lead to a disconnect callback
        // down the line

        if (connection != null) {
            connection.send(msg.serialize());
        }
    }

    protected boolean onClientMsgReceived(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            logger.debug("Packet message failed to parse, shutting down connection");
            disconnect(false);
            return false;
        }

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
                handleMulti(packetMsg);
                break;
            case ClientLogOnResponse: // we handle this to get the SteamID/SessionID and to set up heart beating
                handleLogOnResponse(packetMsg);
                break;
            case ClientLoggedOff: // to stop heart beating when we get logged off
                handleLoggedOff(packetMsg);
                break;
            case ClientServerUnavailable:
                handleServerUnavailable(packetMsg);
                break;
            case ClientSessionToken: // am session token
                handleSessionToken(packetMsg);
                break;
        }

        return true;
    }

    /**
     * Called when the client is securely isConnected to Steam3.
     */
    protected void onClientConnected() {
        ClientMsgProtobuf<CMsgClientHello.Builder> request = new ClientMsgProtobuf<>(CMsgClientHello.class, EMsg.ClientHello);
        request.getBody().setProtocolVersion(MsgClientLogon.CurrentProtocol);

        send(request);
    }

    /**
     * Called when the client is physically disconnected from Steam3.
     *
     * @param userInitiated whether the disconnect was initialized by the client
     */
    protected void onClientDisconnected(boolean userInitiated) {
    }

    private Connection createConnection(EnumSet<ProtocolTypes> protocol) {
        IConnectionFactory connectionFactory = configuration.getConnectionFactory();
        Connection connection = connectionFactory.createConnection(configuration, protocol);
        if (connection == null) {
            logger.error(String.format("Connection factory returned null connection for protocols %s", protocol));
            throw new IllegalArgumentException("Connection factory returned null connection.");
        }
        return connection;
    }

    public static IPacketMsg getPacketMsg(byte[] data) {
        if (data.length < 4) {
            logger.debug("PacketMsg too small to contain a message, was only " + data.length + " bytes. Message: " + Strings.toHex(data));
            return null;
        }

        int rawEMsg = 0;
        try (var reader = new BinaryReader(new ByteArrayInputStream(data))) {
            rawEMsg = reader.readInt();
        } catch (IOException e) {
            logger.debug("Exception while getting EMsg code", e);
        }
        EMsg eMsg = MsgUtil.getMsg(rawEMsg);

        switch (eMsg) {
            case ChannelEncryptRequest:
            case ChannelEncryptResponse:
            case ChannelEncryptResult:
                try {
                    return new PacketMsg(eMsg, data);
                } catch (IOException e) {
                    logger.debug("Exception deserializing emsg " + eMsg + " (" + MsgUtil.isProtoBuf(rawEMsg) + ").", e);
                }
        }

        try {
            if (MsgUtil.isProtoBuf(rawEMsg)) {
                // if the emsg is flagged, we're a proto message
                return new PacketClientMsgProtobuf(eMsg, data);
            } else {
                return new PacketClientMsg(eMsg, data);
            }
        } catch (IOException e) {
            logger.debug("Exception deserializing emsg " + eMsg + " (" + MsgUtil.isProtoBuf(rawEMsg) + ").", e);
            return null;
        }
    }

    private void handleMulti(IPacketMsg packetMsg) {
        if (!packetMsg.isProto()) {
            logger.debug("HandleMulti got non-proto MsgMulti!!");
            return;
        }

        ClientMsgProtobuf<CMsgMulti.Builder> msgMulti = new ClientMsgProtobuf<>(CMsgMulti.class, packetMsg);

        byte[] payload = msgMulti.getBody().getMessageBody().toByteArray();

        if (msgMulti.getBody().getSizeUnzipped() > 0) {
            try (var gzin = new GZIPInputStream(new ByteArrayInputStream(payload));
                 var baos = new ByteArrayOutputStream()) {
                int res = 0;
                byte[] buf = new byte[1024];
                while (res >= 0) {
                    res = gzin.read(buf, 0, buf.length);
                    if (res > 0) {
                        baos.write(buf, 0, res);
                    }
                }
                payload = baos.toByteArray();
            } catch (IOException e) {
                logger.debug("HandleMulti encountered an exception when decompressing.", e);
                return;
            }
        }

        try (var bais = new ByteArrayInputStream(payload);
             var br = new BinaryReader(bais)) {
            while (br.available() > 0) {
                int subSize = br.readInt();
                byte[] subData = br.readBytes(subSize);

                if (!onClientMsgReceived(getPacketMsg(subData))) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("error in handleMulti()", e);
        }
    }

    private void handleLogOnResponse(IPacketMsg packetMsg) {
        if (!packetMsg.isProto()) {
            // a non-proto ClientLogonResponse can come in as a result of connecting but never sending a ClientLogon
            // in this case, it always fails, so we don't need to do anything special here
            logger.debug("Got non-proto logon response, this is indicative of no logon attempt after connecting.");
            return;
        }

        ClientMsgProtobuf<CMsgClientLogonResponse.Builder> logonResp = new ClientMsgProtobuf<>(CMsgClientLogonResponse.class, packetMsg);

        EResult logonResponse = EResult.from(logonResp.getBody().getEresult());
        EResult extendedResponse = EResult.from(logonResp.getBody().getEresultExtended());
        logger.debug("handleLogOnResponse got response: " + logonResponse + ", extended: " + extendedResponse);

        // Note: Sometimes if you sign in too many times, steam may confuse "InvalidPassword" with "RateLimitExceeded"

        if (logonResponse == EResult.OK) {
            sessionID = logonResp.getProtoHeader().getClientSessionid();
            steamID = new SteamID(logonResp.getProtoHeader().getSteamid());

            cellID = logonResp.getBody().getCellId();
            publicIP = NetHelpers.getIPAddress(logonResp.getBody().getPublicIp());
            ipCountryCode = logonResp.getBody().getIpCountryCode();

            // restart heartbeat
            heartBeatFunc.stop();
            heartBeatFunc.setDelay(logonResp.getBody().getLegacyOutOfGameHeartbeatSeconds() * 1000L);
            heartBeatFunc.start();
        } else if (logonResponse == EResult.TryAnotherCM || logonResponse == EResult.ServiceUnavailable) {
            var connection = this.connection;
            if (connection != null) {
                getServers().tryMark(connection.getCurrentEndPoint(), connection.getProtocolTypes(), ServerQuality.BAD);
            } else {
                logger.error("Connection was null trying to mark endpoint bad.");
            }
        }
    }

    private void handleLoggedOff(IPacketMsg packetMsg) {
        sessionID = null;
        steamID = null;

        cellID = null;
        publicIP = null;
        ipCountryCode = null;

        heartBeatFunc.stop();

        if (packetMsg.isProto()) {
            ClientMsgProtobuf<CMsgClientLoggedOff.Builder> logoffMsg = new ClientMsgProtobuf<>(CMsgClientLoggedOff.class, packetMsg);
            EResult logoffResult = EResult.from(logoffMsg.getBody().getEresult());

            logger.debug("handleLoggedOff got " + logoffResult);

            if (logoffResult == EResult.TryAnotherCM || logoffResult == EResult.ServiceUnavailable) {
                var connection = this.connection;
                if (connection != null) {
                    getServers().tryMark(connection.getCurrentEndPoint(), connection.getProtocolTypes(), ServerQuality.BAD);
                } else {
                    logger.error("Connection was null trying to mark endpoint bad.");
                }
            }
        } else {
            logger.debug("handleLoggedOff got unexpected response: " + packetMsg.getMsgType());
        }
    }

    private void handleServerUnavailable(IPacketMsg packetMsg) {
        var msgServerUnavailable = new ClientMsg<>(MsgClientServerUnavailable.class, packetMsg);

        logger.debug("A server of type " + msgServerUnavailable.getBody().getEServerTypeUnavailable() +
                "was not available for request: " + EMsg.from(msgServerUnavailable.getBody().getEMsgSent()));

        disconnect(false);
    }

    private void handleSessionToken(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientSessionToken.Builder> sessToken = new ClientMsgProtobuf<>(CMsgClientSessionToken.class, packetMsg);

        sessionToken = sessToken.getBody().getToken();
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
     * Returns the local IP of this client.
     *
     * @return The local IP or null if no connection is available.
     */
    public @Nullable InetAddress getLocalIP() {
        var connection = this.connection;
        if (connection == null) {
            return null;
        }
        return connection.getLocalIP();
    }

    /**
     * Returns the current endpoint this client is connected to.
     *
     * @return The current endpoint or null if no connection is available.
     */
    public @Nullable InetSocketAddress getCurrentEndpoint() {
        var connection = this.connection;
        if (connection == null) {
            return null;
        }
        return connection.getCurrentEndPoint();
    }

    /**
     * Gets the public IP address of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     *
     * @return The {@link InetSocketAddress} public ip
     */
    public @Nullable InetAddress getPublicIP() {
        return publicIP;
    }

    /**
     * Gets the country code of our public IP address according to Steam. This value is assigned after a logon attempt has succeeded.
     * This value will be <c>null</c> if the client is logged off of Steam.
     *
     * @return the ip country code.
     */
    public @Nullable String getIpCountryCode() {
        return ipCountryCode;
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
     * Gets a value indicating whether this instance is isConnected to the remote CM server.
     *
     * @return <b>true</b> if this instance is isConnected; otherwise, <b>false</b>.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Gets a value indicating whether isConnected and connection is not connected to the remote CM server.
     * Inverse alternative to {@link CMClient#isConnected()}
     *
     * @return <b>true</b> is this instance is disconnected, otherwise, <b>false</b>.
     */
    // > "since the client can technically not be connected but still have a connection"
    public boolean isDisconnected() {
        return !isConnected && connection == null;
    }

    /**
     * @return the session token assigned to this client from the AM.
     */
    public long getSessionToken() {
        return sessionToken;
    }

    /**
     * @return the Steam recommended Cell ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <b>null</b> if the client is logged off of Steam.
     */
    public @Nullable Integer getCellID() {
        return cellID;
    }

    /**
     * Gets the session ID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <b>null</b> if the client is logged off of Steam.
     *
     * @return The session ID.
     */
    public @Nullable Integer getSessionID() {
        return sessionID;
    }

    /**
     * Gets the SteamID of this client. This value is assigned after a logon attempt has succeeded.
     * This value will be <b>null</b> if the client is logged off of Steam.
     *
     * @return The SteamID.
     */
    public @Nullable SteamID getSteamID() {
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
     * @return the network listening interface. Use this for debugging only.
     * For your convenience, you can use {@link NetHookNetworkListener} class.
     */
    public IDebugNetworkListener getDebugNetworkListener() {
        return debugNetworkListener;
    }

    /**
     * Sets the network listening interface. Use this for debugging only.
     * For your convenience, you can use {@link NetHookNetworkListener} class.
     *
     * @param debugNetworkListener the listener
     */
    public void setDebugNetworkListener(IDebugNetworkListener debugNetworkListener) {
        this.debugNetworkListener = debugNetworkListener;
    }

    public boolean isExpectDisconnection() {
        return expectDisconnection;
    }

    public void setExpectDisconnection(boolean expectDisconnection) {
        this.expectDisconnection = expectDisconnection;
    }
}
