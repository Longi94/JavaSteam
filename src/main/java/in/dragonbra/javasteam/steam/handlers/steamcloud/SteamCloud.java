package in.dragonbra.javasteam.steam.handlers.steamcloud;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.*;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.ShareFileCallback;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.SingleFileInfoCallback;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback;
import in.dragonbra.javasteam.types.UGCHandle;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for interacting with remote storage and user generated content.
 */
public class SteamCloud extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamCloud() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientUFSGetUGCDetailsResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleUGCDetailsResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientUFSGetSingleFileInfoResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleSingleFileInfoResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientUFSShareFileResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleShareFileResponse(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Requests details for a specific item of user generated content from the Steam servers.
     * Results are returned in a {@link UGCDetailsCallback}.
     *
     * @param ugcId The unique user generated content id.
     */
    public void requestUGCDetails(UGCHandle ugcId) {
        if (ugcId == null) {
            throw new IllegalArgumentException("ugcId is null");
        }

        ClientMsgProtobuf<CMsgClientUFSGetUGCDetails.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSGetUGCDetails.class, EMsg.ClientUFSGetUGCDetails);
        request.setSourceJobID(client.getNextJobID());

        request.getBody().setHcontent(ugcId.getValue());

        client.send(request);
    }

    /**
     * Requests details for a specific file in the user's Cloud storage.
     * Results are returned in a {@link SingleFileInfoCallback}.
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     */
    public void getSingleFileInfo(int appId, String filename) {
        ClientMsgProtobuf<CMsgClientUFSGetSingleFileInfo.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSGetSingleFileInfo.class, EMsg.ClientUFSGetSingleFileInfo);
        request.setSourceJobID(client.getNextJobID());

        request.getBody().setAppId(appId);
        request.getBody().setFileName(filename);

        client.send(request);
    }

    /**
     * Commit a Cloud file at the given path to make its UGC handle publicly visible.
     * Results are returned in a {@link ShareFileCallback}.
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     */
    public void shareFile(int appId, String filename) {
        ClientMsgProtobuf<CMsgClientUFSShareFile.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSShareFile.class, EMsg.ClientUFSShareFile);
        request.setSourceJobID(client.getNextJobID());

        request.getBody().setAppId(appId);
        request.getBody().setFileName(filename);

        client.send(request);
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

    private void handleUGCDetailsResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUFSGetUGCDetailsResponse.Builder> infoResponse =
                new ClientMsgProtobuf<>(CMsgClientUFSGetUGCDetailsResponse.class, packetMsg);

        client.postCallback(new UGCDetailsCallback(infoResponse.getTargetJobID(), infoResponse.getBody()));
    }

    private void handleSingleFileInfoResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUFSGetSingleFileInfoResponse.Builder> infoResponse =
                new ClientMsgProtobuf<>(CMsgClientUFSGetSingleFileInfoResponse.class, packetMsg);

        client.postCallback(new SingleFileInfoCallback(infoResponse.getTargetJobID(), infoResponse.getBody()));
    }

    private void handleShareFileResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUFSShareFileResponse.Builder> shareResponse =
                new ClientMsgProtobuf<>(CMsgClientUFSShareFileResponse.class, packetMsg);

        client.postCallback(new ShareFileCallback(shareResponse.getTargetJobID(), shareResponse.getBody()));
    }
}
