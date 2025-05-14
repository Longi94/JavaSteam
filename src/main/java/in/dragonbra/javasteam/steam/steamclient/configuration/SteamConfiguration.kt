package `in`.dragonbra.javasteam.steam.steamclient.configuration

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.networking.steam3.IConnectionFactory
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.contentdownloader.IManifestProvider
import `in`.dragonbra.javasteam.steam.discovery.IServerListProvider
import `in`.dragonbra.javasteam.steam.discovery.SmartCMServerList
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.webapi.WebAPI
import `in`.dragonbra.javasteam.util.compat.Consumer
import okhttp3.OkHttpClient
import java.util.*

/**
 * Configuration object to use.
 * This object should not be mutated after it is passed to one or more [SteamClient] objects.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SteamConfiguration internal constructor(private val state: SteamConfigurationState) {

    /**
     * Builds the underlying [in.dragonbra.javasteam.networking.steam3.Connection] used for connecting to stream.
     *
     * ```java
     * steamClient = new SteamClient(SteamConfiguration.create(builder -> {
     *  IConnectionFactory connectionFactory = (configuration, protocol) -> {
     *      return null;//custom connection or null to resolve fallback
     *  };
     *  builder.withConnectionFactory(
     *      connectionFactory.thenResolve(IConnectionFactory.DEFAULT));
     * }));
     * ```
     *
     */
    val connectionFactory: IConnectionFactory
        get() = state.connectionFactory

    /**
     * Whether to use the Steam Directory to discover available servers.
     */
    val isAllowDirectoryFetch: Boolean
        get() = state.isAllowDirectoryFetch

    /**
     * The Steam Cell ID to prioritize when connecting.
     */
    val cellID: Int
        get() = state.cellID

    /**
     * The connection timeout used when connecting to Steam serves.
     */
    val connectionTimeout: Long
        get() = state.connectionTimeout

    /**
     * The http client
     */
    val httpClient: OkHttpClient
        get() = state.httpClient

    /**
     * The default persona state flags used when requesting information for a new friend, or when calling **SteamFriends.RequestFriendInfo** without specifying flags.
     */
    val defaultPersonaStateFlags: EnumSet<EClientPersonaStateFlag>
        get() = state.defaultPersonaStateFlags

    /**
     * The supported protocol types to use when attempting to connect to Steam.
     */
    val protocolTypes: EnumSet<ProtocolTypes>
        get() = state.protocolTypes

    /**
     * The server list provider to use.
     */
    val serverListProvider: IServerListProvider
        get() = state.serverListProvider

    /**
     * The depot manifest provider to use.
     */
    val depotManifestProvider: IManifestProvider
        get() = state.depotManifestProvider

    /**
     * The Universe to connect to. This should always be [EUniverse.Public] unless you work at Valve and are using this internally. If this is you, hello there.
     */
    val universe: EUniverse
        get() = state.universe

    /**
     * The base address of the Steam Web API to connect to. Use of "partner.steam-api.com" requires a Partner API key.
     */
    val webAPIBaseAddress: String
        get() = state.webAPIBaseAddress

    /**
     * An API key to be used for authorized requests. Keys can be obtained from [Steam Web API Documentation](https://steamcommunity.com/dev) or the Steamworks Partner site.
     */
    val webAPIKey: String?
        get() = state.webAPIKey

    /**
     * The server list provider to use.
     */
    val serverList: SmartCMServerList = SmartCMServerList(this)

    /**
     * Retrieves a handler capable of interacting with the specified interface on the Web API.
     *
     * @param interface The interface to retrieve a handler for.
     * A [WebAPI] object to interact with the Web API.
     */
    fun getWebAPI(`interface`: String): WebAPI = WebAPI(httpClient, webAPIBaseAddress, `interface`, webAPIKey)

    companion object {
        /**
         * Creates a [SteamConfiguration], allowing for configuration.
         *
         * @param configurator A method which is used to configure the configuration.
         * A configuration object.
         */
        @JvmStatic
        fun create(configurator: Consumer<ISteamConfigurationBuilder>): SteamConfiguration {
            val builder = SteamConfigurationBuilder()

            configurator.accept(builder)

            return builder.build()
        }

        @JvmStatic
        fun createDefault(): SteamConfiguration = SteamConfiguration(SteamConfigurationBuilder.createDefaultState())
    }
}
