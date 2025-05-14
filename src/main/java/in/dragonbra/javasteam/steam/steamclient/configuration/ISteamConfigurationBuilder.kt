package `in`.dragonbra.javasteam.steam.steamclient.configuration

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.networking.steam3.IConnectionFactory
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.contentdownloader.IManifestProvider
import `in`.dragonbra.javasteam.steam.discovery.IServerListProvider
import okhttp3.OkHttpClient
import java.util.*

/**
 * Interface to configure a [SteamConfiguration] before it is created.
 * A reference to the underlying object should not be live beyond the configurator function's scope.
 */
@Suppress("unused")
interface ISteamConfigurationBuilder {

    /**
     * Configures this [SteamConfiguration] to use the provided [IConnectionFactory].
     *
     * @param connectionFactory The [IConnectionFactory] to use.
     * @return A builder with modified configuration.
     */
    fun withConnectionFactory(connectionFactory: IConnectionFactory): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] for a particular Steam cell.
     *
     * @param cellID The Steam Cell ID to prioritize when connecting.
     * @return A builder with modified configuration.
     */
    fun withCellID(cellID: Int): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] with a connection timeout.
     *
     * @param connectionTimeout The connection timeout used when connecting to Steam serves.
     * @return A builder with modified configuration.
     */
    fun withConnectionTimeout(connectionTimeout: Long): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] with custom HTTP behaviour.
     *
     * @param httpClient the http client
     * @return A builder with modified configuration.
     */
    fun withHttpClient(httpClient: OkHttpClient): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] with the default [EClientPersonaStateFlag]s to request from Steam.
     *
     * @param personaStateFlags The default persona state flags used when requesting information for a new friend, or when calling **SteamFriends.RequestFriendInfo** without specifying flags.
     * @return A builder with modified configuration.
     */
    fun withDefaultPersonaStateFlags(personaStateFlags: EnumSet<EClientPersonaStateFlag>): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] with the default [EClientPersonaStateFlag]s to request from Steam.
     *
     * @param personaStateFlags The default persona state flags used when requesting information for a new friend, or when calling **SteamFriends.RequestFriendInfo** without specifying flags.
     * @return A builder with modified configuration.
     */
    fun withDefaultPersonaStateFlags(personaStateFlags: EClientPersonaStateFlag): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] to discover available servers.
     *
     * @param allowDirectoryFetch Whether or not to use the Steam Directory to discover available servers.
     * @return A builder with modified configuration.
     */
    fun withDirectoryFetch(allowDirectoryFetch: Boolean): ISteamConfigurationBuilder

    /**
     * Configures how this [SteamConfiguration] will be used to connect to Steam.
     *
     * @param protocolTypes The supported protocol types to use when attempting to connect to Steam.
     * @return A builder with modified configuration.
     */
    fun withProtocolTypes(protocolTypes: EnumSet<ProtocolTypes>): ISteamConfigurationBuilder

    /**
     * Configures how this [SteamConfiguration] will be used to connect to Steam.
     *
     * @param protocolTypes The supported protocol types to use when attempting to connect to Steam.
     * @return A builder with modified configuration.
     */
    fun withProtocolTypes(protocolTypes: ProtocolTypes): ISteamConfigurationBuilder

    /**
     * Configures the server list provider for this [SteamConfiguration].
     *
     * @param provider The server list provider to use.
     * @return A builder with modified configuration.
     */
    fun withServerListProvider(provider: IServerListProvider): ISteamConfigurationBuilder

    /**
     * Configures the depot manifest provider for this [SteamConfiguration].
     *
     * @param provider The depot manifest provider to use.
     * @return A builder with modified configuration.
     */
    fun withManifestProvider(provider: IManifestProvider): ISteamConfigurationBuilder

    /**
     * Configures the Universe that this [SteamConfiguration] belongs to.
     *
     * @param universe The Universe to connect to. This should always be [EUniverse.Public] unless you work at Valve and are using this internally. If this is you, hello there.
     * @return A builder with modified configuration.
     */
    fun withUniverse(universe: EUniverse): ISteamConfigurationBuilder

    /**
     * Configures the Steam Web API address for this [SteamConfiguration].
     *
     * @param baseAddress The base address of the Steam Web API to connect to. Use of "partner.steam-api.com" requires a Partner API Key.
     * @return A builder with modified configuration.
     */
    fun withWebAPIBaseAddress(baseAddress: String): ISteamConfigurationBuilder

    /**
     * Configures this [SteamConfiguration] with a Web API key to attach to requests.
     *
     * @param webApiKey An API key to be used for authorized requests. Keys can be obtained from [Steam Web API Documentation](https://steamcommunity.com/dev) or the Steamworks Partner site.
     * @return A builder with modified configuration.
     */
    fun withWebAPIKey(webApiKey: String): ISteamConfigurationBuilder
}
