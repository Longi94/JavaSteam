package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.steam.webapi.SteamDirectory;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Smart list of CM servers.
 */
@SuppressWarnings("unused")
public class SmartCMServerList {

    private static final Logger logger = LogManager.getLogger(SmartCMServerList.class);

    private final SteamConfiguration configuration;

    private final List<ServerInfo> servers = Collections.synchronizedList(new ArrayList<>());

    private Duration badConnectionMemoryTimeSpan;

    public SmartCMServerList(SteamConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration is null");
        }

        this.configuration = configuration;
        this.badConnectionMemoryTimeSpan = Duration.ofMinutes(5);
    }

    private void startFetchingServers() throws IOException {
        // if the server list has been populated, no need to perform any additional work
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
        final long cutoff = System.currentTimeMillis() - badConnectionMemoryTimeSpan.toMillis();

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

        var distinctEndPoints = endPoints.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        servers.clear();

        for (ServerRecord distinctEndPoint : distinctEndPoints) {
            addCore(distinctEndPoint);
        }

        configuration.getServerListProvider().updateServerList(endPoints);
    }

    private void addCore(ServerRecord endPoint) {
        for (ProtocolTypes protocolType : endPoint.getProtocolTypes()) {
            var info = new ServerInfo(endPoint, protocolType);
            servers.add(info);
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
        var serverInfos = new ArrayList<ServerInfo>();

        if (quality == ServerQuality.GOOD) {
            serverInfos = servers.stream()
                    .filter(x -> x.getRecord().getEndpoint().equals(endPoint) && protocolTypes.contains(x.getProtocol()))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else {
            // If we're marking this server for any failure, mark all endpoints for the host at the same time
            var host = endPoint.getHostString();
            serverInfos = servers.stream()
                    .filter(x -> x.getRecord().getHost().equals(host))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (serverInfos.isEmpty()) {
            return false;
        }

        for (ServerInfo serverInfo : serverInfos) {
            logger.debug("Marking " + serverInfo.getRecord().getEndpoint() + " - " + serverInfo.getProtocol() + " as " + quality);
            markServerCore(serverInfo, quality);
        }

        return true;
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

        var index = new AtomicInteger(0);

        var result = servers.stream()
                .filter(server -> supportedProtocolTypes.contains(server.getProtocol()))
                .map(server -> new AbstractMap.SimpleEntry<>(server, index.getAndIncrement()))
                .sorted(Comparator
                        .comparing((AbstractMap.SimpleEntry<ServerInfo, Integer> entry) ->
                                Optional.ofNullable(entry.getKey().getLastBadConnection()).orElse(new Date(0)))
                        .thenComparing(AbstractMap.SimpleEntry::getValue))
                .map(AbstractMap.SimpleEntry::getKey)
                .findFirst();

        if (result.isEmpty()) {
            return null;
        }

        var server = result.get();
        return new ServerRecord(server.getRecord().getEndpoint(), server.getProtocol());
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
        var endPoints = new ArrayList<ServerRecord>();

        try {
            startFetchingServers();
        } catch (IOException e) {
            return new ArrayList<>();
        }

        endPoints = servers.stream().map(ServerInfo::getRecord).distinct().collect(Collectors.toCollection(ArrayList::new));

        return endPoints;
    }

    public long getBadConnectionMemoryTimeSpan() {
        return badConnectionMemoryTimeSpan.toMillis();
    }

    public void setBadConnectionMemoryTimeSpan(long badConnectionMemoryTimeSpan) {
        this.badConnectionMemoryTimeSpan = Duration.ofMillis(badConnectionMemoryTimeSpan);
    }
}
