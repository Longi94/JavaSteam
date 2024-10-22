package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests SteamConfiguration with set values
 */
public class SteamConfigurationConfiguredObjectTest {

    private final SteamConfiguration configuration = SteamConfiguration.create(builder ->
            builder.withDirectoryFetch(false)
                    .withCellID(123)
                    .withConnectionTimeout(60000L)
                    .withDefaultPersonaStateFlags(EClientPersonaStateFlag.SourceID)
                    .withHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES).build())
                    .withProtocolTypes(EnumSet.of(ProtocolTypes.WEB_SOCKET, ProtocolTypes.UDP))
                    .withServerListProvider(new CustomServerListProvider())
                    .withUniverse(EUniverse.Internal)
                    .withWebAPIBaseAddress("https://foo.bar.com/api/")
                    .withWebAPIKey("T0PS3kR1t")
    );

    @Test
    public void DirectoryFetchIsConfigured() {
        Assertions.assertFalse(configuration.isAllowDirectoryFetch());
    }

    @Test
    public void CellIDIsConfigured() {
        Assertions.assertEquals(123, configuration.getCellID());
    }

    @Test
    public void ConnectionTimeoutIsConfigured() {
        Assertions.assertEquals(60000L, configuration.getConnectionTimeout());
    }

    @Test
    public void HttpClientFactoryIsConfigured() {
        var client = configuration.getHttpClient();
        Assertions.assertEquals(60000, client.connectTimeoutMillis());
    }

    // @Test
    // public void MachineInfoProviderIsConfigured() {
    // }

    @Test
    public void PersonaStateFlagsIsConfigured() {
        Assertions.assertEquals(EnumSet.of(EClientPersonaStateFlag.SourceID), configuration.getDefaultPersonaStateFlags());
    }

    @Test
    public void ServerListProviderIsConfigured() {
        Assertions.assertInstanceOf(CustomServerListProvider.class, configuration.getServerListProvider());
    }

    @Test
    public void ServerListIsNotNull() {
        Assertions.assertNotNull(configuration.getServerList());
    }

    @Test
    public void ProtocolsAreConfigured() {
        Assertions.assertEquals(EnumSet.of(ProtocolTypes.WEB_SOCKET, ProtocolTypes.UDP), configuration.getProtocolTypes());
    }

    @Test
    public void UniverseIsConfigured() {
        Assertions.assertEquals(EUniverse.Internal, configuration.getUniverse());
    }

    @Test
    public void WebAPIAddress() {
        Assertions.assertEquals("https://foo.bar.com/api/", configuration.getWebAPIBaseAddress());
    }

    @Test
    public void NoWebAPIKey() {
        Assertions.assertEquals("T0PS3kR1t", configuration.getWebAPIKey());
    }

    static class CustomServerListProvider implements IServerListProvider {
        @Override
        public @NotNull Instant getLastServerListRefresh() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ServerRecord> fetchServerList() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateServerList(List<ServerRecord> endpoints) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
