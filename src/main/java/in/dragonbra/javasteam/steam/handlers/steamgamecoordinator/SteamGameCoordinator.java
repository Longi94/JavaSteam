package in.dragonbra.javasteam.steam.handlers.steamgamecoordinator;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback;
import in.dragonbra.javasteam.util.MsgUtil;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler handles all game coordinator messaging.
 */
public class SteamGameCoordinator extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamGameCoordinator() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientFromGC, this::handleFromGC);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Sends a game coordinator message for a specific appid.
     *
     * @param msg   The GC message to send.
     * @param appId The app id of the game coordinator to send to.
     */
    public void send(IClientGCMsg msg, int appId) {
        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        ClientMsgProtobuf<CMsgGCClient.Builder> clientMsg = new ClientMsgProtobuf<>(CMsgGCClient.class, EMsg.ClientToGC);

        clientMsg.getProtoHeader().setRoutingAppid(appId);
        clientMsg.getBody().setMsgtype(MsgUtil.makeGCMsg(msg.getMsgType(), msg.isProto()));
        clientMsg.getBody().setAppid(appId);

        clientMsg.getBody().setPayload(ByteString.copyFrom(msg.serialize()));

        client.send(clientMsg);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    private void handleFromGC(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgGCClient.Builder> msg = new ClientMsgProtobuf<>(CMsgGCClient.class, packetMsg);

        client.postCallback(new MessageCallback(msg.getTargetJobID(), msg.getBody()));
    }
}
