package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;
import in.dragonbra.javasteam.steam.discovery.NullServerListProvider;
import in.dragonbra.javasteam.steam.webapi.WebAPI;

import java.util.EnumSet;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class SteamConfigurationBuilder implements ISteamConfigurationBuilder {

    private SteamConfigurationState state;

    public SteamConfigurationBuilder() {
        state = createDefaultState();
    }

    public static SteamConfigurationState createDefaultState() {
        SteamConfigurationState state = new SteamConfigurationState();

        state.setAllowDirectoryFetch(true);
        state.setConnectionTimeout(5000L);
        state.setDefaultPersonaStateFlags(EnumSet.of(EClientPersonaStateFlag.PlayerName, EClientPersonaStateFlag.Presence,
                EClientPersonaStateFlag.SourceID, EClientPersonaStateFlag.GameExtraInfo, EClientPersonaStateFlag.LastSeen));
        state.setProtocolTypes(ProtocolTypes.TCP);
        state.setServerListProvider(new NullServerListProvider());
        state.setUniverse(EUniverse.Public);
        state.setWebAPIBaseAddress(WebAPI.DEFAULT_BASE_ADDRESS);

        return state;
    }

    public SteamConfiguration build() {
        return new SteamConfiguration(state);
    }

    @Override
    public ISteamConfigurationBuilder withCellID(int cellID) {
        state.setCellID(cellID);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withConnectionTimeout(long connectionTimeout) {
        state.setConnectionTimeout(connectionTimeout);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withDefaultPersonaStateFlags(EnumSet<EClientPersonaStateFlag> personaStateFlags) {
        state.setDefaultPersonaStateFlags(personaStateFlags);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withDefaultPersonaStateFlags(EClientPersonaStateFlag personaStateFlags) {
        state.setDefaultPersonaStateFlags(personaStateFlags);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withDirectoryFetch(boolean allowDirectoryFetch) {
        state.setAllowDirectoryFetch(allowDirectoryFetch);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withProtocolTypes(EnumSet<ProtocolTypes> protocolTypes) {
        state.setProtocolTypes(protocolTypes);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withProtocolTypes(ProtocolTypes protocolTypes) {
        state.setProtocolTypes(protocolTypes);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withServerListProvider(IServerListProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider is null");
        }
        state.setServerListProvider(provider);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withUniverse(EUniverse universe) {
        state.setUniverse(universe);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withWebAPIBaseAddress(String baseAddress) {
        if (baseAddress == null) {
            throw new IllegalArgumentException("baseAddress is null");
        }
        state.setWebAPIBaseAddress(baseAddress);
        return this;
    }

    @Override
    public ISteamConfigurationBuilder withWebAPIKey(String webApiKey) {
        if (webApiKey == null) {
            throw new IllegalArgumentException("webApiKey is null");
        }
        state.setWebAPIKey(webApiKey);
        return this;
    }
}
