package in.dragonbra.javasteam.steam.handlers.steamnetworking;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.*;
import in.dragonbra.javasteam.steam.handlers.steamnetworking.callback.NetworkingCertificateCallback;
import in.dragonbra.javasteam.types.AsyncJobSingle;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for Steam networking sockets
 */
public class SteamNetworking extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamNetworking() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientNetworkingCertRequestResponse, this::handleNetworkingCertRequestResponse);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Request a signed networking certificate from Steam for your Ed25519 public key for the given app id.
     * Results are returned in a {@link NetworkingCertificateCallback}.
     * The returned {@link AsyncJobSingle} can also be awaited to retrieve the callback result.
     *
     * @param appId     The App ID the certificate will be generated for.
     * @param publicKey Your Ed25519 public key.
     * @return The Job ID of the request. This can be used to find the appropriate {@link NetworkingCertificateCallback}.
     */
    public AsyncJobSingle<NetworkingCertificateCallback> requestNetworkingCertificate(int appId, byte[] publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey is null");
        }

        ClientMsgProtobuf<CMsgClientNetworkingCertRequest.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientNetworkingCertRequest.class, EMsg.ClientNetworkingCertRequest);

        msg.setSourceJobID(client.getNextJobID());

        msg.getBody().setAppId(appId);
        msg.getBody().setKeyData(ByteString.copyFrom(publicKey));

        client.send(msg);

        return new AsyncJobSingle<>(this.client, msg.getSourceJobID());
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        // ignore messages that we don't have a handler function for
        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    void handleNetworkingCertRequestResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientNetworkingCertReply.Builder> resp =
                new ClientMsgProtobuf<>(CMsgClientNetworkingCertReply.class, packetMsg);

        NetworkingCertificateCallback callback = new NetworkingCertificateCallback(resp.getTargetJobID(), resp.getBody());
        client.postCallback(callback);
    }
}
