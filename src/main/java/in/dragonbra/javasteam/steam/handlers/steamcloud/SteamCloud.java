package in.dragonbra.javasteam.steam.handlers.steamcloud;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetSingleFileInfo;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetSingleFileInfoResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetails;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetailsResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFile;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSShareFileResponse;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.ShareFileCallback;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.SingleFileInfoCallback;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback;
import in.dragonbra.javasteam.types.JobID;
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

        dispatchMap.put(EMsg.ClientUFSGetUGCDetailsResponse, this::handleUGCDetailsResponse);
        dispatchMap.put(EMsg.ClientUFSGetSingleFileInfoResponse, this::handleSingleFileInfoResponse);
        dispatchMap.put(EMsg.ClientUFSShareFileResponse, this::handleShareFileResponse);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Requests details for a specific item of user generated content from the Steam servers.
     * Results are returned in a {@link UGCDetailsCallback}.
     *
     * @param ugcId The unique user generated content id.
     * @return The Job ID of the request. This can be used to find the appropriate {@link UGCDetailsCallback}.
     */
    public JobID requestUGCDetails(UGCHandle ugcId) {
        if (ugcId == null) {
            throw new IllegalArgumentException("ugcId is null");
        }

        ClientMsgProtobuf<CMsgClientUFSGetUGCDetails.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSGetUGCDetails.class, EMsg.ClientUFSGetUGCDetails);
        JobID jobID = client.getNextJobID();
        request.setSourceJobID(jobID);

        request.getBody().setHcontent(ugcId.getValue());

        client.send(request);

        return jobID;
    }

    /**
     * Requests details for a specific file in the user's Cloud storage.
     * Results are returned in a {@link SingleFileInfoCallback}.
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate {@link SingleFileInfoCallback}.
     */
    public JobID getSingleFileInfo(int appId, String filename) {
        ClientMsgProtobuf<CMsgClientUFSGetSingleFileInfo.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSGetSingleFileInfo.class, EMsg.ClientUFSGetSingleFileInfo);
        JobID jobID = client.getNextJobID();
        request.setSourceJobID(jobID);

        request.getBody().setAppId(appId);
        request.getBody().setFileName(filename);

        client.send(request);

        return jobID;
    }

    /**
     * Commit a Cloud file at the given path to make its UGC handle publicly visible.
     * Results are returned in a {@link ShareFileCallback}.
     *
     * @param appId    The app id of the game.
     * @param filename The path to the file being requested.
     * @return The Job ID of the request. This can be used to find the appropriate {@link ShareFileCallback}.
     */
    public JobID shareFile(int appId, String filename) {
        ClientMsgProtobuf<CMsgClientUFSShareFile.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientUFSShareFile.class, EMsg.ClientUFSShareFile);
        JobID jobID = client.getNextJobID();
        request.setSourceJobID(jobID);

        request.getBody().setAppId(appId);
        request.getBody().setFileName(filename);

        client.send(request);

        return jobID;
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
