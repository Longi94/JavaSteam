package `in`.dragonbra.javasteam.steam.handlers

import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback

/**
 * This class implements the base requirements every message handler should inherit from.
 *
 * @constructor Initializes a new instance of the [ClientMsgHandler] class.
 */
abstract class ClientMsgHandler {

    /**
     * Gets the underlying [SteamClient] for use in sending replies.
     */
    protected lateinit var client: SteamClient

    fun setup(client: SteamClient) {
        this.client = client
    }

    /**
     * Gets or Sets whether the related [SteamClient] should imminently expect the server to close the connection.
     * If this is true when the connection is closed, the [DisconnectedCallback]'s
     * [DisconnectedCallback.isUserInitiated] property will be set to **true**.
     */
    protected var isExpectDisconnection: Boolean
        get() = client.expectDisconnection
        set(expectDisconnection) {
            client.expectDisconnection = expectDisconnection
        }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    abstract fun handleMsg(packetMsg: IPacketMsg)
}
