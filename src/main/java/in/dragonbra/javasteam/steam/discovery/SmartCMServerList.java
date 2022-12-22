package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.steam.webapi.SteamDirectory;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Smart list of CM servers.
 */
public class SmartCMServerList {

    private static final Logger logger = LogManager.getLogger(SmartCMServerList.class);

    private final SteamConfiguration configuration;

    private List<ServerInfo> servers = Collections.synchronizedList(new ArrayList<>());

    private Long badConnectionMemoryTimeSpan;

    public SmartCMServerList(SteamConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration is null");
        }

        this.configuration = configuration;
    }

    private void startFetchingServers() throws IOException {
        if (!servers.isEmpty()) {
            return;
        }

        resolveServerList();
    }

    private void resolveServerList() throws IOException {
        logger.debug("Resolving server list");

        List<ServerRecord> endPoints = configuration.getServerListProvider().fetchServerList();
        if (endPoints == null) {
            endPoints = new ArrayList<>();
        }

        if (endPoints.isEmpty() && configuration.isAllowDirectoryFetch()) {
            logger.debug("Server list provider had no entries, will query SteamDirectory");
            endPoints = SteamDirectory.load(configuration);
        }

        if (endPoints.isEmpty() && configuration.isAllowDirectoryFetch()) {
            logger.debug("Could not query SteamDirectory, falling back to cm2-ord1");

            // Grabbed a random host that is not an IP address from the endpoint list.
            InetSocketAddress cm0 = new InetSocketAddress("cm2-ord1.cm.steampowered.com", 27017);
            endPoints.add(ServerRecord.createSocketServer(cm0));
        }

        logger.debug("Resolved " + endPoints.size() + " servers");
        replaceList(endPoints);
    }

    /**
     * Resets the scores of all servers which has a last bad connection more than {@link SmartCMServerList#badConnectionMemoryTimeSpan} ago.
     */
    public void resetOldScores() {
        if (badConnectionMemoryTimeSpan == null) {
            return;
        }

        final long cutoff = System.currentTimeMillis() - badConnectionMemoryTimeSpan;

        for (ServerInfo serverInfo : servers) {
            if (serverInfo.getLastBadConnection() != null && serverInfo.getLastBadConnection().getTime() < cutoff) {
                serverInfo.setLastBadConnection(null);
            }
        }
    }

    /**
     * Replace the list with a new list of servers provided to us by the Steam servers.
     *
     * @param endPoints The {@link ServerRecord ServerRecords} to use for this {@link SmartCMServerList}.
     */
    public void replaceList(List<ServerRecord> endPoints) {
        if (endPoints == null) {
            throw new IllegalArgumentException("endPoints is null");
        }

        servers.clear();

        for (ServerRecord endPoint : endPoints) {
            addCore(endPoint);
        }

        configuration.getServerListProvider().updateServerList(endPoints);
    }

    private void addCore(ServerRecord endPoint) {
        for (ProtocolTypes protocol : endPoint.getProtocolTypes()) {
            servers.add(new ServerInfo(endPoint, protocol));
        }
    }

    /**
     * Explicitly resets the known state of all servers.
     */
    public void resetBadServers() {
        for (ServerInfo serverInfo : servers) {
            serverInfo.setLastBadConnection(null);
        }
    }

    public boolean tryMark(InetSocketAddress endPoint, ProtocolTypes protocolTypes, ServerQuality quality) {
        return tryMark(endPoint, EnumSet.of(protocolTypes), quality);
    }

    public boolean tryMark(InetSocketAddress endPoint, EnumSet<ProtocolTypes> protocolTypes, ServerQuality quality) {
        List<ServerInfo> serverInfos = new ArrayList<>();
        for (ServerInfo x : servers) {
            if (x.getRecord().getEndpoint().equals(endPoint) && protocolTypes.contains(x.getProtocol())) {
                serverInfos.add(x);
            }
        }

        for (ServerInfo serverInfo : serverInfos) {
            logger.debug("Marking " + serverInfo.getRecord().getEndpoint() + " - " + serverInfo.getProtocol() + " as " + quality);
            markServerCore(serverInfo, quality);
        }

        return serverInfos.size() > 0;
    }

    private void markServerCore(ServerInfo serverInfo, ServerQuality quality) {
        switch (quality) {
            case GOOD:
                serverInfo.setLastBadConnection(null);
                break;
            case BAD:
                serverInfo.setLastBadConnection(new Date());
                break;
        }
    }

    /**
     * Perform the actual score lookup of the server list and return the candidate.
     *
     * @param supportedProtocolTypes The minimum supported {@link ProtocolTypes} of the server to return.
     * @return An {@link ServerRecord}, or null if the list is empty.
     */
    private ServerRecord getNextServerCandidateInternal(EnumSet<ProtocolTypes> supportedProtocolTypes) {
        resetOldScores();

        List<ServerInfo> serverInfos = new ArrayList<>();
        for (ServerInfo serverInfo : servers) {
            if (supportedProtocolTypes.contains(serverInfo.getProtocol())) {
                serverInfos.add(serverInfo);
            }
        }

        //noinspection ComparatorMethodParameterNotUsed
        serverInfos.sort((o1, o2) -> {
            if (o1.getLastBadConnection() == null && o2.getLastBadConnection() == null) {
                return 1;
            }

            if (o1.getLastBadConnection() == null) {
                return -1;
            }

            if (o2.getLastBadConnection() == null) {
                return 1;
            }

            return o1.getLastBadConnection().before(o2.getLastBadConnection()) ? -1 : 1;
        });

        if (serverInfos.isEmpty()) {
            return null;
        }

        ServerInfo result = serverInfos.get(0);

        return new ServerRecord(result.getRecord().getEndpoint(), result.getProtocol());
    }

    /**
     * Get the next server in the list.
     *
     * @param supportedProtocolTypes The minimum supported {@link ProtocolTypes} of the server to return.
     * @return An {@link ServerRecord}, or null if the list is empty.
     */
    public ServerRecord getNextServerCandidate(EnumSet<ProtocolTypes> supportedProtocolTypes) {
        try {
            startFetchingServers();
        } catch (IOException e) {
            return null;
        }

        return getNextServerCandidateInternal(supportedProtocolTypes);
    }

    /**
     * Get the next server in the list.
     *
     * @param supportedProtocolTypes The minimum supported {@link ProtocolTypes} of the server to return.
     * @return An {@link ServerRecord}, or null if the list is empty.
     */
    public ServerRecord getNextServerCandidate(ProtocolTypes supportedProtocolTypes) {
        return getNextServerCandidate(EnumSet.of(supportedProtocolTypes));
    }

    /**
     * Gets the {@link ServerRecord ServerRecords} of all servers in the server list.
     *
     * @return An {@link List} array contains the {@link ServerRecord ServerRecords} of the servers in the list
     */
    public List<ServerRecord> getAllEndPoints() {
        try {
            startFetchingServers();
        } catch (IOException e) {
            return new ArrayList<>();
        }

        List<ServerRecord> serverRecords = new ArrayList<>();

        for (ServerInfo server : servers) {
            ServerRecord record = server.getRecord();
            if (!serverRecords.contains(record)) {
                serverRecords.add(record);
            }
        }

        return serverRecords;
    }

    public long getBadConnectionMemoryTimeSpan() {
        return badConnectionMemoryTimeSpan;
    }

    public void setBadConnectionMemoryTimeSpan(long badConnectionMemoryTimeSpan) {
        this.badConnectionMemoryTimeSpan = badConnectionMemoryTimeSpan;
    }
}
