package in.dragonbra.javasteam.steam.handlers.steammasterserver;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgClientGMSServerQuery;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGMSClientServerQueryResponse;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.callback.QueryCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.NetHelpers;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for requesting server list details from Steam.
 */
public class SteamMasterServer extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamMasterServer() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.GMSClientServerQueryResponse, this::handleServerQueryResponse);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Requests a list of servers from the Steam game master server.
     * Results are returned in a {@link QueryCallback}.
     *
     * @param details The details for the request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link QueryCallback}.
     */
    public JobID serverQuery(QueryDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientGMSServerQuery.Builder> query =
                new ClientMsgProtobuf<>(CMsgClientGMSServerQuery.class, EMsg.ClientGMSServerQuery);
        JobID jobID = client.getNextJobID();
        query.setSourceJobID(jobID);

        query.getBody().setAppId(details.getAppID());

        if (details.getGeoLocatedIP() != null) {
            query.getBody().setGeoLocationIp(NetHelpers.getIPAddress(details.getGeoLocatedIP()));
        }

        query.getBody().setFilterText(details.getFilter());
        query.getBody().setRegionCode(details.getRegion().code());

        query.getBody().setMaxServers(details.getMaxServers());

        client.send(query);

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

    private void handleServerQueryResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgGMSClientServerQueryResponse.Builder> queryResponse =
                new ClientMsgProtobuf<>(CMsgGMSClientServerQueryResponse.class, packetMsg);

        client.postCallback(new QueryCallback(queryResponse.getTargetJobID(), queryResponse.getBody()));
    }
}
