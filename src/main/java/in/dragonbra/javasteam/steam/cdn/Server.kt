package `in`.dragonbra.javasteam.steam.cdn

import java.net.InetSocketAddress

/**
 * Represents a single Steam3 'Steampipe' content server.
 */
class Server {

    companion object {
        /**
         * Creates a Server from an InetSocketAddress.
         */
        @JvmStatic
        fun fromInetSocketAddress(endPoint: InetSocketAddress): Server = Server().apply {
            protocol = if (endPoint.port == 443) ConnectionProtocol.HTTPS else ConnectionProtocol.HTTP
            host = endPoint.address.hostAddress
            vHost = endPoint.address.hostAddress
            port = endPoint.port
        }

        /**
         * Creates a Server from hostname and port.
         */
        @JvmStatic
        fun fromHostAndPort(hostname: String, port: Int): Server = Server().apply {
            protocol = if (port == 443) ConnectionProtocol.HTTPS else ConnectionProtocol.HTTP
            host = hostname
            vHost = hostname
            this.port = port
        }
    }

    /**
     * The protocol used to connect to this server
     */
    enum class ConnectionProtocol {
        /**
         * Server does not advertise HTTPS support, connect over HTTP
         */
        HTTP,

        /**
         * Server advertises it supports HTTPS, connection made over HTTPS
         */
        HTTPS,
    }

    /**
     * Gets the supported connection protocol of the server.
     */
    var protocol: ConnectionProtocol = ConnectionProtocol.HTTP
        internal set

    /**
     * Gets the hostname of the server.
     */
    var host: String? = null
        internal set

    /**
     * Gets the virtual hostname of the server.
     */
    var vHost: String? = null
        internal set

    /**
     * Gets the port of the server.
     */
    var port: Int = 0
        internal set

    /**
     * Gets the type of the server.
     */
    var type: String? = null
        internal set

    /**
     * Gets the SourceID this server belongs to.
     */
    var sourceId: Int = 0
        internal set

    /**
     * Gets the CellID this server belongs to.
     */
    var cellId: Int = 0
        internal set

    /**
     * Gets the load value associated with this server.
     */
    var load: Int = 0
        internal set

    /**
     * Gets the weighted load.
     */
    var weightedLoad: Float = 0F
        internal set

    /**
     * Gets the number of entries this server is worth.
     */
    var numEntries: Int = 0
        internal set

    /**
     * Gets the flag whether this server is for Steam China only.
     */
    var steamChinaOnly: Boolean = false
        internal set

    /**
     * Gets the download proxy status.
     */
    var useAsProxy: Boolean = false
        internal set

    /**
     * Gets the transformation template applied to request paths.
     */
    var proxyRequestPathTemplate: String? = null
        internal set

    /**
     * Gets the list of app ids this server can be used with.
     */
    var allowedAppIds: IntArray = intArrayOf()
        internal set

    /**
     * Returns a string that represents this server.
     */
    override fun toString(): String = "$host:$port ($type)"
}
