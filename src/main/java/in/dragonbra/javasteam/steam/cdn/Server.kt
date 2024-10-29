package `in`.dragonbra.javasteam.steam.cdn

/**
 * Represents a single Steam3 'Steampipe' content server.
 */
class Server(
    protocol: ConnectionProtocol = ConnectionProtocol.HTTP,
    host: String,
    vHost: String,
    port: Int,
    type: String? = null,
    sourceID: Int = 0,
    cellID: Int = 0,
    load: Int = 0,
    weightedLoad: Float = 0f,
    numEntries: Int = 0,
    steamChinaOnly: Boolean = false,
    useAsProxy: Boolean = false,
    proxyRequestPathTemplate: String? = null,
    allowedAppIds: IntArray = IntArray(0),
) {
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
        HTTPS
    }

    /**
     * Gets the supported connection protocol of the server.
     */
    var protocol = protocol
        internal set
    /**
     * Gets the hostname of the server.
     */
    var host = host
        internal set
    /**
     * Gets the virtual hostname of the server.
     */
    var vHost = vHost
        internal set
    /**
     * Gets the port of the server.
     */
    var port = port
        internal set

    /**
     * Gets the type of the server.
     */
    var type = type
        internal set

    /**
     * Gets the SourceID this server belongs to.
     */
    var sourceID = sourceID
        internal set

    /**
     * Gets the CellID this server belongs to.
     */
    var cellID = cellID
        internal set

    /**
     * Gets the load value associated with this server.
     */
    var load = load
        internal set
    /**
     * Gets the weighted load.
     */
    var weightedLoad = weightedLoad
        internal set
    /**
     * Gets the number of entries this server is worth.
     */
    var numEntries = numEntries
        internal set
    /**
     * Gets the flag whether this server is for Steam China only.
     */
    var steamChinaOnly = steamChinaOnly
        internal set
    /**
     * Gets the download proxy status.
     */
    var useAsProxy = useAsProxy
        internal set
    /**
     * Gets the transformation template applied to request paths.
     */
    var proxyRequestPathTemplate = proxyRequestPathTemplate
        internal set

    /**
     * Gets the list of app ids this server can be used with.
     */
    var allowedAppIds = allowedAppIds
        internal set

    /**
     * Returns a string that represents this server.
     */
    override fun toString(): String {
        return "$host:$port ($type)"
    }
}
