package in.dragonbra.javasteam.steam.steamclient.callbacks;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientCMList;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This callback is received when the client has received the CM list from Steam.
 */
public class CMListCallback extends CallbackMsg {

    private final Collection<ServerRecord> servers;

    public CMListCallback(CMsgClientCMList.Builder cmMsg) {
        List<Integer> addresses = cmMsg.getCmAddressesList();
        List<Integer> ports = cmMsg.getCmPortsList();
        List<ServerRecord> cmList = IntStream.range(0, Math.min(addresses.size(), ports.size()))
                .mapToObj(i -> ServerRecord.createSocketServer(new InetSocketAddress(NetHelpers.getIPAddress(addresses.get(i)), ports.get(i))))
                .collect(Collectors.toList());

        List<ServerRecord> webSocketList = cmMsg.getCmWebsocketAddressesList().stream()
                .map(ServerRecord::createWebSocketServer)
                .collect(Collectors.toList());

        cmList.addAll(webSocketList);

        servers = Collections.unmodifiableCollection(cmList);
    }

    /**
     * Gets the CM server list.
     */
    public Collection<ServerRecord> getServers() {
        return servers;
    }
}
