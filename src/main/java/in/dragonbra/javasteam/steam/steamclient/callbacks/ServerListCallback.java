package in.dragonbra.javasteam.steam.steamclient.callbacks;

import in.dragonbra.javasteam.enums.EServerType;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientServerList;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * This callback is fired when the client receives a list of all publically available Steam3 servers.
 * This callback may be fired multiple times for different server lists.
 */
public class ServerListCallback extends CallbackMsg {

    private final Map<EServerType, Collection<InetSocketAddress>> servers = new HashMap<>();

    public ServerListCallback(CMsgClientServerList.Builder serverList) {
        for (CMsgClientServerList.Server s : serverList.getServersList()) {
            EServerType type = EServerType.from(s.getServerType());

            Collection<InetSocketAddress> addresses = servers.get(type);
            if (addresses == null) {
                addresses = new ArrayList<>();
                servers.put(type, addresses);
            }

            addresses.add(new InetSocketAddress(
                    NetHelpers.getIPAddress(s.getServerIp()), s.getServerPort()
            ));
        }
    }

    public Map<EServerType, Collection<InetSocketAddress>> getServers() {
        return servers;
    }
}
