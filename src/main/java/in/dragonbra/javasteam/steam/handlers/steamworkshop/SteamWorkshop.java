package in.dragonbra.javasteam.steam.handlers.steamworkshop;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgCREEnumeratePublishedFiles;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgCREEnumeratePublishedFilesResponse;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserPublishedFiles;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserPublishedFilesResponse;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserSubscribedFiles;
import in.dragonbra.javasteam.protobufs.steamclient.FriendsMobile.CMsgClientUCMEnumerateUserSubscribedFilesResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.*;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.callback.PublishedFilesCallback;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.callback.UserActionPublishedFilesCallback;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.callback.UserPublishedFilesCallback;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.callback.UserSubscribedFilesCallback;
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

        dispatchMap.put(EMsg.CREEnumeratePublishedFilesResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleEnumPublishedFiles(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientUCMEnumerateUserPublishedFilesResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleEnumUserPublishedFiles(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientUCMEnumerateUserSubscribedFilesResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleEnumUserSubscribedFiles(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientUCMEnumeratePublishedFilesByUserActionResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleEnumPublishedFilesByAction(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Enumerates the list of published files for the current logged in user.
     * Results are returned in a {@link UserPublishedFilesCallback}.
     *
     * @param details The specific details of the request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link UserPublishedFilesCallback}.
     */
    public JobID enumerateUserPublishedFiles(EnumerationUserDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientUCMEnumerateUserPublishedFiles.Builder> enumRequest =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumerateUserPublishedFiles.class, EMsg.ClientUCMEnumerateUserPublishedFiles);
        JobID jobID = client.getNextJobID();
        enumRequest.setSourceJobID(jobID);

        enumRequest.getBody().setAppId(details.getAppID());
        enumRequest.getBody().setSortOrder(details.getSortOrder());
        enumRequest.getBody().setStartIndex(details.getStartIndex());

        client.send(enumRequest);

        return jobID;
    }

    /**
     * Enumerates the list of subscribed files for the current logged in user.
     * Results are returned in a {@link UserSubscribedFilesCallback}.
     *
     * @param details The specific details of the request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link UserSubscribedFilesCallback}.
     */
    public JobID enumerateUserSubscribedFiles(EnumerationUserDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientUCMEnumerateUserSubscribedFiles.Builder> enumRequest =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumerateUserSubscribedFiles.class, EMsg.ClientUCMEnumerateUserSubscribedFiles);
        JobID jobID = client.getNextJobID();
        enumRequest.setSourceJobID(jobID);

        enumRequest.getBody().setAppId(details.getAppID());
        enumRequest.getBody().setStartIndex(details.getStartIndex());

        client.send(enumRequest);

        return jobID;
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

    /**
     * Enumerates the list of all published files on the Steam workshop.
     * Results are returned in a {@link PublishedFilesCallback}.
     *
     * @param details The specific details of the request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link PublishedFilesCallback}.
     */
    public JobID enumeratePublishedFiles(EnumerationDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgCREEnumeratePublishedFiles.Builder> enumRequest =
                new ClientMsgProtobuf<>(CMsgCREEnumeratePublishedFiles.class, EMsg.CREEnumeratePublishedFiles);
        JobID jobID = client.getNextJobID();
        enumRequest.setSourceJobID(jobID);

        enumRequest.getBody().setAppId(details.getAppID());
        enumRequest.getBody().setQueryType(details.getType().code());
        enumRequest.getBody().setStartIndex(details.getStartIndex());
        enumRequest.getBody().setDays(details.getDays());
        enumRequest.getBody().setCount(details.getCount());
        enumRequest.getBody().addAllTags(details.getTags());
        enumRequest.getBody().addAllUserTags(details.getUserTags());

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

    private void handleEnumPublishedFiles(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgCREEnumeratePublishedFilesResponse.Builder> response =
                new ClientMsgProtobuf<>(CMsgCREEnumeratePublishedFilesResponse.class, packetMsg);

        client.postCallback(new PublishedFilesCallback(response.getTargetJobID(), response.getBody()));
    }

    private void handleEnumUserPublishedFiles(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUCMEnumerateUserPublishedFilesResponse.Builder> response =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumerateUserPublishedFilesResponse.class, packetMsg);

        client.postCallback(new UserPublishedFilesCallback(response.getTargetJobID(), response.getBody()));
    }

    private void handleEnumUserSubscribedFiles(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUCMEnumerateUserSubscribedFilesResponse.Builder> response =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumerateUserSubscribedFilesResponse.class, packetMsg);

        client.postCallback(new UserSubscribedFilesCallback(response.getTargetJobID(), response.getBody()));
    }

    private void handleEnumPublishedFilesByAction(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.Builder> response =
                new ClientMsgProtobuf<>(CMsgClientUCMEnumeratePublishedFilesByUserActionResponse.class, packetMsg);

        client.postCallback(new UserActionPublishedFilesCallback(response.getTargetJobID(), response.getBody()));
    }
}
