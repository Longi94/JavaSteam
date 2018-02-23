package in.dragonbra.javasteam.handlers;

import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;

/**
 * This class implements the base requirements every message handler should inherit from.
 */
public abstract class ClientMsgHandler {

    protected SteamClient client;

    protected boolean expectDisconnection;

    public void setup(SteamClient client) {
        this.client = client;
    }

    /**
     * Gets whether or not the related {@link SteamClient} should imminently expect the server to close the connection.
     * If this is true when the connection is closed, the {@link SteamClient#DisconnectedCallback}'s
     * {@link SteamClient#DisconnectedCallback#userInitiated} property will be set to <b>true</b>.
     */
    public boolean isExpectDisconnection() {
        return client.isExpectDisconnection();
    }

    /**
     * Sets whether or not the related {@link SteamClient} should imminently expect the server to close the connection.
     * If this is true when the connection is closed, the {@link SteamClient#DisconnectedCallback}'s
     * {@link SteamClient#DisconnectedCallback#userInitiated} property will be set to <b>true</b>.
     */
    public void setExpectDisconnection(boolean expectDisconnection) {
        client.setExpectDisconnection(expectDisconnection);
    }

    /**
     * Gets the underlying {@link SteamClient} for use in sending replies.
     */
    public SteamClient getClient() {
        return client;
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    public abstract void handleMsg(IPacketMsg packetMsg);
}
