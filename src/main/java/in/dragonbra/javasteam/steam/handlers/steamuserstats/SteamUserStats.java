package in.dragonbra.javasteam.steam.handlers.steamuserstats;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.ELeaderboardDataRequest;
import in.dragonbra.javasteam.enums.ELeaderboardDisplayType;
import in.dragonbra.javasteam.enums.ELeaderboardSortMethod;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgDPGetNumberOfCurrentPlayers;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgDPGetNumberOfCurrentPlayersResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSFindOrCreateLB;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSFindOrCreateLBResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntries;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLbs.CMsgClientLBSGetLBEntriesResponse;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.callback.FindOrCreateLeaderboardCallback;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.callback.LeaderboardEntriesCallback;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.callback.NumberOfPlayersCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler handles Steam user statistic related actions.
 */
public class SteamUserStats extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamUserStats() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientGetNumberOfCurrentPlayersDPResponse, this::handleNumberOfPlayersResponse);
        dispatchMap.put(EMsg.ClientLBSFindOrCreateLBResponse, this::handleFindOrCreateLBResponse);
        dispatchMap.put(EMsg.ClientLBSGetLBEntriesResponse, this::handleGetLBEntriesResponse);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Retrieves the number of current players for a given app id.
     * Results are returned in a {@link NumberOfPlayersCallback}.
     *
     * @param appId The app id to request the number of players for.
     * @return The Job ID of the request. This can be used to find the appropriate {@link NumberOfPlayersCallback}.
     */
    public JobID getNumberOfCurrentPlayers(int appId) {
        ClientMsgProtobuf<CMsgDPGetNumberOfCurrentPlayers.Builder> msg =
                new ClientMsgProtobuf<>(CMsgDPGetNumberOfCurrentPlayers.class, EMsg.ClientGetNumberOfCurrentPlayersDP);
        JobID jobID = client.getNextJobID();
        msg.setSourceJobID(jobID);

        msg.getBody().setAppid(appId);

        client.send(msg);

        return jobID;
    }

    /**
     * Asks the Steam back-end for a leaderboard by name for a given appid.
     * Results are returned in a {@link FindOrCreateLeaderboardCallback}.
     *
     * @param appId The AppID to request a leaderboard for.
     * @param name  Name of the leaderboard to request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link FindOrCreateLeaderboardCallback}.
     */
    public JobID findLeaderBoard(int appId, String name) {
        ClientMsgProtobuf<CMsgClientLBSFindOrCreateLB.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientLBSFindOrCreateLB.class, EMsg.ClientLBSFindOrCreateLB);
        JobID jobID = client.getNextJobID();
        msg.setSourceJobID(jobID);

        // routing_appid has to be set correctly to receive a response
        msg.getProtoHeader().setRoutingAppid(appId);

        msg.getBody().setAppId(appId);
        msg.getBody().setLeaderboardName(name);
        msg.getBody().setCreateIfNotFound(false);

        client.send(msg);

        return jobID;
    }

    /**
     * Asks the Steam back-end for a leaderboard by name, and will create it if it's not yet.
     * Results are returned in a {@link FindOrCreateLeaderboardCallback}.
     *
     * @param appId       The AppID to request a leaderboard for.
     * @param name        Name of the leaderboard to create.
     * @param sortMethod  Sort method to use for this leaderboard
     * @param displayType Display type for this leaderboard.
     * @return The Job ID of the request. This can be used to find the appropriate {@link FindOrCreateLeaderboardCallback}.
     */
    public JobID createLeaderboard(int appId, String name, ELeaderboardSortMethod sortMethod, ELeaderboardDisplayType displayType) {
        ClientMsgProtobuf<CMsgClientLBSFindOrCreateLB.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientLBSFindOrCreateLB.class, EMsg.ClientLBSFindOrCreateLB);
        JobID jobID = client.getNextJobID();
        msg.setSourceJobID(jobID);

        // routing_appid has to be set correctly to receive a response
        msg.getProtoHeader().setRoutingAppid(appId);

        msg.getBody().setAppId(appId);
        msg.getBody().setLeaderboardName(name);
        msg.getBody().setLeaderboardDisplayType(displayType.code());
        msg.getBody().setLeaderboardSortMethod(sortMethod.code());
        msg.getBody().setCreateIfNotFound(true);

        client.send(msg);

        return jobID;
    }

    /**
     * Asks the Steam back-end for a set of rows in the leaderboard.
     * Results are returned in a {@link LeaderboardEntriesCallback}.
     *
     * @param appId       The AppID to request leaderboard rows for.
     * @param id          ID of the leaderboard to view.
     * @param rangeStart  Range start or 0.
     * @param rangeEnd    Range end or max leaderboard entries.
     * @param dataRequest Type of request.
     * @return The Job ID of the request. This can be used to find the appropriate {@link LeaderboardEntriesCallback}.
     */
    public JobID getLeaderboardEntries(int appId, int id, int rangeStart, int rangeEnd, ELeaderboardDataRequest dataRequest) {
        ClientMsgProtobuf<CMsgClientLBSGetLBEntries.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientLBSGetLBEntries.class, EMsg.ClientLBSGetLBEntries);
        JobID jobID = client.getNextJobID();
        msg.setSourceJobID(jobID);

        msg.getBody().setAppId(appId);
        msg.getBody().setLeaderboardId(id);
        msg.getBody().setLeaderboardDataRequest(dataRequest.code());
        msg.getBody().setRangeStart(rangeStart);
        msg.getBody().setRangeEnd(rangeEnd);

        client.send(msg);

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

    private void handleNumberOfPlayersResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgDPGetNumberOfCurrentPlayersResponse.Builder> msg =
                new ClientMsgProtobuf<>(CMsgDPGetNumberOfCurrentPlayersResponse.class, packetMsg);

        client.postCallback(new NumberOfPlayersCallback(msg.getTargetJobID(), msg.getBody()));
    }

    private void handleFindOrCreateLBResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientLBSFindOrCreateLBResponse.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientLBSFindOrCreateLBResponse.class, packetMsg);

        client.postCallback(new FindOrCreateLeaderboardCallback(msg.getTargetJobID(), msg.getBody()));
    }

    private void handleGetLBEntriesResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientLBSGetLBEntriesResponse.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientLBSGetLBEntriesResponse.class, packetMsg);

        client.postCallback(new LeaderboardEntriesCallback(msg.getTargetJobID(), msg.getBody()));
    }
}
