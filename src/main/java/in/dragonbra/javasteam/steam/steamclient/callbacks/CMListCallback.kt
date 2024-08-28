package in.dragonbra.javasteam.steam.steamclient.callbacks;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientCMList;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.util.NetHelpers;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This callback is received when the client has received the CM list from Steam.
 */
public class CMListCallback extends CallbackMsg {

    private final Collection<ServerRecord> servers;

    public CMListCallback(CMsgClientCMList.Builder cmMsg) {
        List<Integer> addresses = cmMsg.getCmAddressesList();
        List<Integer> ports = cmMsg.getCmPortsList();

        List<ServerRecord> cmList = new ArrayList<>();
        for (int i = 0; i < Math.min(addresses.size(), ports.size()); i++) {
            cmList.add(ServerRecord.createSocketServer(new InetSocketAddress(NetHelpers.getIPAddress(addresses.get(i)), ports.get(i))));
        }

        for (String s : cmMsg.getCmWebsocketAddressesList()) {
            cmList.add(ServerRecord.createWebSocketServer(s));
        }

        servers = Collections.unmodifiableCollection(cmList);
    }

    /**
     * @return the CM server list.
     */
    public Collection<ServerRecord> getServers() {
        return servers;
    }
}
