package `in`.dragonbra.javasteam.steam.discovery

import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.util.NetHelpers
import java.net.InetSocketAddress
import java.util.EnumSet

/**
 * Represents the information needed to connect to a CM server
 *
 * @constructor
 * @param endpoint The endpoint of the server to connect to.
 * @param protocolTypes The various protocol types that can be used to communicate with this server.
 */
class ServerRecord private constructor(
    val endpoint: InetSocketAddress,
    val protocolTypes: EnumSet<ProtocolTypes>,
) {

    /**
     *
     */
    internal constructor(endpoint: InetSocketAddress, protocolTypes: ProtocolTypes) :
        this(endpoint, EnumSet.of(protocolTypes))

    /**
     * Gets the host of the associated endpoint.
     */
    val host: String = endpoint.hostString

    /**
     * The endpoint of the server to connect to.
     */
    val port: Int = endpoint.port

    /**
     * Determines whether the specified object is equal to the current object.
     * @param other the object to compare to.
     * @return true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is ServerRecord) {
            return false
        }

        return endpoint == other.endpoint && protocolTypes == other.protocolTypes
    }

    /**
     * Hash function
     * @return A hash code for the current object.
     */
    override fun hashCode(): Int = endpoint.hashCode() xor protocolTypes.hashCode()

    companion object {
        /**
         * Creates a server record for a given endpoint.
         * @param host The host to connect to.
         * @param port The port to connect to.
         * @param protocolTypes The protocol types that this server supports.
         * @return A new [ServerRecord] instance
         */
        @JvmStatic
        fun createServer(host: String, port: Int, protocolTypes: ProtocolTypes): ServerRecord =
            createServer(host, port, EnumSet.of<ProtocolTypes>(protocolTypes))

        /**
         * Creates a server record for a given endpoint.
         * @param host The host to connect to.
         * @param port The port to connect to.
         * @param protocolTypes The protocol types that this server supports.
         * @return A new [ServerRecord] instance
         */
        @JvmStatic
        fun createServer(host: String, port: Int, protocolTypes: EnumSet<ProtocolTypes>): ServerRecord =
            ServerRecord(InetSocketAddress(host, port), protocolTypes)

        /**
         * Creates a Socket server given an IP endpoint.
         * @param endpoint The IP address and port of the server.
         * @return A new [ServerRecord] instance
         */
        @JvmStatic
        fun createSocketServer(endpoint: InetSocketAddress): ServerRecord =
            ServerRecord(endpoint, EnumSet.of<ProtocolTypes>(ProtocolTypes.TCP, ProtocolTypes.UDP))

        /**
         * Creates a Socket server given an IP endpoint.
         * @param address The IP address and port of the server, as a string.
         * @return A new [ServerRecord], if the address was able to be parsed. **null** otherwise.
         */
        @JvmStatic
        fun tryCreateSocketServer(address: String): ServerRecord? {
            var endpoint = NetHelpers.tryParseIPEndPoint(address) ?: return null

            return ServerRecord(endpoint, EnumSet.of<ProtocolTypes>(ProtocolTypes.TCP, ProtocolTypes.UDP))
        }

        /**
         * Creates a WebSocket server given an address in the form of "hostname:port".
         * @param address The name and port of the server
         * @return A new [ServerRecord] instance
         */
        @JvmStatic
        fun createWebSocketServer(address: String): ServerRecord {
            val defaultPort = 443

            val split = address.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var endpoint = if (split.size > 1) {
                InetSocketAddress(split[0], split[1].toInt())
            } else {
                InetSocketAddress(address, defaultPort)
            }

            return ServerRecord(endpoint, ProtocolTypes.WEB_SOCKET)
        }
    }
}
