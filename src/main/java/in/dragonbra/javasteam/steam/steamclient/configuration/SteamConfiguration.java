package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;
import in.dragonbra.javasteam.steam.discovery.SmartCMServerList;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;

import java.util.function.Consumer;

/**
 * Configuration object to use.
 * This object should not be mutated after it is passed to one or more {@link SteamClient} objects.
 */
public class SteamConfiguration {

    private final SteamConfigurationState state;

    private SmartCMServerList serverList;

    SteamConfiguration(SteamConfigurationState state) {
        this.state = state;
        this.serverList = new SmartCMServerList(this);
    }

    /**
     * Creates a {@link SteamConfiguration}, allowing for configuration.
     *
     * @param configurator A method which is used to configure the configuration.
     * @return A configuration object.
     */
    public static SteamConfiguration create(Consumer<ISteamConfigurationBuilder> configurator) {
        if (configurator == null) {
            throw new IllegalArgumentException("configurator is null");
        }

        SteamConfigurationBuilder builder = new SteamConfigurationBuilder();

        configurator.accept(builder);

        return builder.build();
    }

    public static SteamConfiguration createDefault() {
        return new SteamConfiguration(SteamConfigurationBuilder.createDefaultState());
    }

    /**
     * @return Whether or not to use the Steam Directory to discover available servers.
     */
    public boolean isAllowDirectoryFetch() {
        return state.isAllowDirectoryFetch();
    }

    /**
     * @return The Steam Cell ID to prioritize when connecting.
     */
    public int getCellID() {
        return state.getCellID();
    }

    /**
     * @return The connection timeout used when connecting to Steam serves.
     */
    public long getConnectionTimeout() {
        return state.getConnectionTimeout();
    }

    /**
     * @return The default persona state flags used when requesting information for a new friend, or when calling <c>SteamFriends.RequestFriendInfo</c> without specifying flags.
     */
    public int getDefaultPersonaStateFlags() {
        return state.getDefaultPersonaStateFlags();
    }

    /**
     * @return The supported protocol types to use when attempting to connect to Steam.
     */
    public ProtocolTypes getProtocolTypes() {
        return state.getProtocolTypes();
    }

    /**
     * @return The server list provider to use.
     */
    public IServerListProvider getServerListProvider() {
        return state.getServerListProvider();
    }

    /**
     * @return The Universe to connect to. This should always be {@link EUniverse#Public} unless you work at Valve and are using this internally. If this is you, hello there.
     */
    public EUniverse getUniverse() {
        return state.getUniverse();
    }

    /**
     * @return The base address of the Steam Web API to connect to. Use of "partner.steam-api.com" requires a Partner API key.
     */
    public String getWebAPIBaseAddress() {
        return state.getWebAPIBaseAddress();
    }

    /**
     * @return An API key to be used for authorized requests. Keys can be obtained from https://steamcommunity.com/dev or the Steamworks Partner site.
     */
    public String getWebAPIKey() {
        return state.getWebAPIKey();
    }

    /**
     * @return The server list used for this configuration. If this configuration is used by multiple {@link SteamClient} instances, they all share the server list.
     */
    public SmartCMServerList getServerList() {
        return serverList;
    }
}
