package in.dragonbra.javasteam.steam.handlers.steammasterserver.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverGameservers.CMsgGMSClientServerQueryResponse;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.QueryDetails;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.Server;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is received in response to calling {@link SteamMasterServer#serverQuery(QueryDetails)}.
 */
public class QueryCallback extends CallbackMsg {

    private List<Server> servers;

    public QueryCallback(JobID jobID, CMsgGMSClientServerQueryResponse.Builder msg) {
        setJobID(jobID);

        List<Server> serverList = new ArrayList<>();
        for (CMsgGMSClientServerQueryResponse.Server s : msg.getServersList()) {
            serverList.add(new Server(s));
        }

        servers = Collections.unmodifiableList(serverList);
    }

    /**
     * @return the list of servers
     */
    public List<Server> getServers() {
        return servers;
    }
}
