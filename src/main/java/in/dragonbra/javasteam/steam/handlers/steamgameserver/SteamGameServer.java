package in.dragonbra.javasteam.steam.handlers.steamgameserver;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EServerFlags;
import in.dragonbra.javasteam.generated.MsgClientLogon;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientTicketAuthComplete;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgGSServerType;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgGSStatusReply;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogOff;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogon;
import in.dragonbra.javasteam.steam.handlers.steamgameserver.callback.StatusReplyCallback;
import in.dragonbra.javasteam.steam.handlers.steamgameserver.callback.TicketAuthCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.HardwareUtils;
import in.dragonbra.javasteam.util.NetHelpers;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.Utils;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.net.Inet6Address;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for interacting with the Steam network as a game server.
 */
public class SteamGameServer extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamGameServer() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.GSStatusReply, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleStatusReply(packetMsg);
            }
        });

        dispatchMap.put(EMsg.ClientTicketAuthComplete, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleAuthComplete(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Logs onto the Steam network as a persistent game server.
     * The client should already have been connected at this point.
     * Results are return in a {@link LoggedOnCallback}.
     *
     * @param details The details to use for logging on.
     */
    public void logOn(LogOnDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        if (Strings.isNullOrEmpty(details.getToken())) {
            throw new IllegalArgumentException("LogOn requires a game server token to be set in 'details'.");
        }

        if (!client.isConnected()) {
            client.postCallback(new LoggedOnCallback(EResult.NoConnection));
            return;
        }

        ClientMsgProtobuf<CMsgClientLogon.Builder> logon =
                new ClientMsgProtobuf<>(CMsgClientLogon.class, EMsg.ClientLogonGameServer);

        SteamID gsId = new SteamID(0, 0, client.getUniverse(), EAccountType.GameServer);

        logon.getProtoHeader().setClientSessionid(0);
        logon.getProtoHeader().setSteamid(gsId.convertToUInt64());

        int localIp = NetHelpers.getIPAddress(client.getLocalIP());
        logon.getBody().setObfustucatedPrivateIp(localIp ^ MsgClientLogon.ObfuscationMask);

        logon.getBody().setProtocolVersion(MsgClientLogon.CurrentProtocol);

        logon.getBody().setClientOsType(Utils.getOSType().code());
        logon.getBody().setGameServerAppId(details.getAppID());
        logon.getBody().setMachineId(ByteString.copyFrom(HardwareUtils.getMachineID()));

        logon.getBody().setGameServerToken(details.getToken());

        client.send(logon);
    }

    /**
     * Logs the client into the Steam3 network as an anonymous game server.
     * The client should already have been connected at this point.
     * Results are return in a {@link LoggedOnCallback}.
     */
    public void logOnAnonymous() {
        logOnAnonymous(0);
    }

    /**
     * Logs the client into the Steam3 network as an anonymous game server.
     * The client should already have been connected at this point.
     * Results are return in a {@link LoggedOnCallback}.
     *
     * @param appId The AppID served by this game server, or 0 for the default.
     */
    public void logOnAnonymous(int appId) {

        if (!client.isConnected()) {
            client.postCallback(new LoggedOnCallback(EResult.NoConnection));
            return;
        }

        ClientMsgProtobuf<CMsgClientLogon.Builder> logon =
                new ClientMsgProtobuf<>(CMsgClientLogon.class, EMsg.ClientLogonGameServer);

        SteamID gsId = new SteamID(0, 0, client.getUniverse(), EAccountType.AnonGameServer);

        logon.getProtoHeader().setClientSessionid(0);
        logon.getProtoHeader().setSteamid(gsId.convertToUInt64());

        int localIp = NetHelpers.getIPAddress(client.getLocalIP());
        logon.getBody().setObfustucatedPrivateIp(localIp ^ MsgClientLogon.ObfuscationMask);

        logon.getBody().setProtocolVersion(MsgClientLogon.CurrentProtocol);

        logon.getBody().setClientOsType(Utils.getOSType().code());
        logon.getBody().setGameServerAppId(appId);
        logon.getBody().setMachineId(ByteString.copyFrom(HardwareUtils.getMachineID()));

        client.send(logon);
    }

    /**
     * Informs the Steam servers that this client wishes to log off from the network.
     * The Steam server will disconnect the client, and a {@link DisconnectedCallback} will be posted.
     */
    public void logOff() {
        expectDisconnection = true;

        ClientMsgProtobuf<CMsgClientLogOff.Builder> logOff = new ClientMsgProtobuf<>(CMsgClientLogOff.class, EMsg.ClientLogOff);
        client.send(logOff);

        // TODO: 2018-02-28 it seems like the socket is not closed after getting logged of or I am doing something horribly wrong, let's disconnect here
        client.disconnect();
    }

    /**
     * Sends the server's status to the Steam network.
     * Results are returned in a {@link StatusReplyCallback} callback.
     * @param details A {@link StatusDetails} object containing the server's status.
     */
    public void sendStatus(StatusDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        if (details.getAddress() != null && details.getAddress() instanceof Inet6Address) {
            throw new IllegalArgumentException("Only IPv4 addresses are supported.");
        }

        ClientMsgProtobuf<CMsgGSServerType.Builder> status = new ClientMsgProtobuf<>(CMsgGSServerType.class, EMsg.GSServerType);
        status.getBody().setAppIdServed(details.getAppID());
        status.getBody().setFlags(EServerFlags.code(details.getServerFlags()));
        status.getBody().setGameDir(details.getGameDirectory());
        status.getBody().setGamePort(details.getPort());
        status.getBody().setGameQueryPort(details.getQueryPort());
        status.getBody().setGameVersion(details.getVersion());

        if (details.getAddress() != null) {
            status.getBody().setGameIpAddress(NetHelpers.getIPAddress(details.getAddress()));
        }

        client.send(status);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        if (dispatchMap.containsKey(packetMsg.getMsgType())) {
            dispatchMap.get(packetMsg.getMsgType()).accept(packetMsg);
        }
    }

    private void handleStatusReply(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgGSStatusReply.Builder> statusReply =
                new ClientMsgProtobuf<>(CMsgGSStatusReply.class, packetMsg);

        client.postCallback(new StatusReplyCallback(statusReply.getBody()));
    }

    private void handleAuthComplete(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientTicketAuthComplete.Builder> statusReply =
                new ClientMsgProtobuf<>(CMsgClientTicketAuthComplete.class, packetMsg);

        client.postCallback(new TicketAuthCallback(statusReply.getBody()));
    }
}
