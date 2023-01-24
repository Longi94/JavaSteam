package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;
import okhttp3.OkHttpClient;

import java.util.EnumSet;

/**
 * Interface to configure a {@link SteamConfiguration} before it is created.
 * A reference to the underlying object should not be live beyond the configurator function's scope.
 */
public interface ISteamConfigurationBuilder {

    /**
     * Configures this {@link SteamConfiguration} for a particular Steam cell.
     *
     * @param cellID The Steam Cell ID to prioritize when connecting.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withCellID(int cellID);

    /**
     * Configures this {@link SteamConfiguration} with a connection timeout.
     *
     * @param connectionTimeout The connection timeout used when connecting to Steam serves.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withConnectionTimeout(long connectionTimeout);

    /**
     * Configures this {@link SteamConfiguration} with custom HTTP behaviour.
     *
     * @param httpClient the http client
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withHttpClient(OkHttpClient httpClient);

    /**
     * Configures this {@link SteamConfiguration} with the default {@link EClientPersonaStateFlag}s to request from Steam.
     *
     * @param personaStateFlags The default persona state flags used when requesting information for a new friend, or when calling <b>SteamFriends.RequestFriendInfo</b> without specifying flags.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withDefaultPersonaStateFlags(EnumSet<EClientPersonaStateFlag> personaStateFlags);

    /**
     * Configures this {@link SteamConfiguration} with the default {@link EClientPersonaStateFlag}s to request from Steam.
     *
     * @param personaStateFlags The default persona state flags used when requesting information for a new friend, or when calling <b>SteamFriends.RequestFriendInfo</b> without specifying flags.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withDefaultPersonaStateFlags(EClientPersonaStateFlag personaStateFlags);

    /**
     * Configures this {@link SteamConfiguration} to discover available servers.
     *
     * @param allowDirectoryFetch Whether or not to use the Steam Directory to discover available servers.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withDirectoryFetch(boolean allowDirectoryFetch);

    /**
     * Configures how this {@link SteamConfiguration} will be used to connect to Steam.
     *
     * @param protocolTypes The supported protocol types to use when attempting to connect to Steam.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withProtocolTypes(EnumSet<ProtocolTypes> protocolTypes);

    /**
     * Configures how this {@link SteamConfiguration} will be used to connect to Steam.
     *
     * @param protocolTypes The supported protocol types to use when attempting to connect to Steam.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withProtocolTypes(ProtocolTypes protocolTypes);

    /**
     * Configures the server list provider for this {@link SteamConfiguration}.
     *
     * @param provider The server list provider to use..
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withServerListProvider(IServerListProvider provider);

    /**
     * Configures the Universe that this {@link SteamConfiguration} belongs to.
     *
     * @param universe The Universe to connect to. This should always be {@link EUniverse#Public} unless you work at Valve and are using this internally. If this is you, hello there.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withUniverse(EUniverse universe);

    /**
     * Configures the Steam Web API address for this {@link SteamConfiguration}.
     *
     * @param baseAddress The base address of the Steam Web API to connect to. Use of "partner.steam-api.com" requires a Partner API Key.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withWebAPIBaseAddress(String baseAddress);

    /**
     * Configures this {@link SteamConfiguration} with a Web API key to attach to requests.
     *
     * @param webApiKey An API key to be used for authorized requests. Keys can be obtained from <a href="https://steamcommunity.com/dev">Steam Web API Documentation</a> or the Steamworks Partner site.
     * @return A builder with modified configuration.
     */
    ISteamConfigurationBuilder withWebAPIKey(String webApiKey);
}
