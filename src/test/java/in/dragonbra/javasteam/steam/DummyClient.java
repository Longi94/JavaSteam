package in.dragonbra.javasteam.steam;

import in.dragonbra.javasteam.base.IClientMsg;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class DummyClient extends CMClient {
    public DummyClient() {
        super(SteamConfiguration.createDefault());
    }

    public void dummyDisconnect() {
        disconnect();
        onClientDisconnected(true);
    }

    public void handleClientMsg(IClientMsg clientMsg) {
        onClientMsgReceived(getPacketMsg(clientMsg.serialize()));
    }
}
