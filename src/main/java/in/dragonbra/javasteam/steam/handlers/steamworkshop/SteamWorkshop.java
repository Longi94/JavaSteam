package in.dragonbra.javasteam.steam.handlers.steamworkshop;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMEnumeratePublishedFilesByUserAction;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUcm.CMsgClientUCMEnumeratePublishedFilesByUserActionResponse;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.callback.UserActionPublishedFilesCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for requesting files published on the Steam Workshop.
 */
public class SteamWorkshop extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamWorkshop() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientUCMEnumeratePublishedFilesByUserActionResponse, this::handleEnumPublishedFilesByAction);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Enumerates the list of published files for the current logged in user based on user action.
     * Results are returned in a {@link UserActionPublishedFilesCallback}.
     *
     * @param details The specific details of the request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link UserActionPublishedFilesCallback}.
     */
    public JobID enumeratePublishedFilesByUserAction(EnumerationUserDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientUCMEnumeratePublishedFilesByUserAction.Builder> enumRequest =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumeratePublishedFilesByUserAction.class, EMsg.ClientUCMEnumeratePublishedFilesByUserAction);
        JobID jobID = client.getNextJobID();
        enumRequest.setSourceJobID(jobID);

        enumRequest.getBody().setAction(details.getUserAction().code());
        enumRequest.getBody().setAppId(details.getAppID());
        enumRequest.getBody().setStartIndex(details.getStartIndex());

        client.send(enumRequest);

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

    private void handleEnumPublishedFilesByAction(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.Builder> response =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.class, packetMsg);

        client.postCallback(new UserActionPublishedFilesCallback(response.getTargetJobID(), response.getBody()));
    }
}
