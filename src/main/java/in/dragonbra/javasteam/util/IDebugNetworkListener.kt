package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.enums.EMsg

/**
 * This is a debug utility, do not use it to implement your business logic.
 * This interface is used for logging network messages sent to and received from the Steam server that the client is connected to.
 */
interface IDebugNetworkListener {
    /**
     * Called when a packet is received from the Steam server.
     * @param msgType Network message type of this packet message.
     * @param data    Raw packet data that was received.
     */
    fun onIncomingNetworkMessage(msgType: EMsg, data: ByteArray)

    /**
     * Called when a packet is about to be sent to the Steam server.
     * @param msgType Network message type of this packet message.
     * @param data    Raw packet data that was received.
     */
    fun onOutgoingNetworkMessage(msgType: EMsg, data: ByteArray)
}
