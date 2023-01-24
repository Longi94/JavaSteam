package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;
import in.dragonbra.javasteam.steam.discovery.MemoryServerListProvider;
import in.dragonbra.javasteam.steam.discovery.ServerRecord;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SteamConfigurationTest {

    private final SteamConfiguration defaultConfig = SteamConfiguration.createDefault();

    private final SteamConfiguration modifiedConfig = SteamConfiguration.create(builder ->
            builder.withDirectoryFetch(false)
            .withCellID(123)
            .withConnectionTimeout(60000L)
            .withDefaultPersonaStateFlags(EClientPersonaStateFlag.SourceID)
            .withHttpClient(new OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES).build())
            .withProtocolTypes(EnumSet.of(ProtocolTypes.WEB_SOCKET, ProtocolTypes.UDP))
            .withServerListProvider(new CustomServerListProvider())
            .withUniverse(EUniverse.Internal)
            .withWebAPIBaseAddress("http://foo.bar.com/api/")
            .withWebAPIKey("T0PS3kR1t")
    );

    @Test
    public void allowDirectoryFetch() {
        assertTrue(defaultConfig.isAllowDirectoryFetch());
    }

    @Test
    public void cellIDIsZero() {
        assertEquals(0, defaultConfig.getCellID());
    }

    @Test
    public void connectionTimeoutIsFiveSeconds() {
        assertEquals(5000L, defaultConfig.getConnectionTimeout());
    }

    @Test
    public void defaultPersonaStateFlags() {
        EnumSet<EClientPersonaStateFlag> expected = EnumSet.of(
                EClientPersonaStateFlag.PlayerName,
                EClientPersonaStateFlag.Presence,
                EClientPersonaStateFlag.SourceID,
                EClientPersonaStateFlag.GameExtraInfo,
                EClientPersonaStateFlag.LastSeen
        );
        assertEquals(expected, defaultConfig.getDefaultPersonaStateFlags());
    }

    @Test
    public void defaultHttpClient() {
        assertNotNull(defaultConfig.getHttpClient());
    }

    @Test
    public void serverListProviderIsNothingFancy() {
        assertTrue(defaultConfig.getServerListProvider() instanceof MemoryServerListProvider);
    }

    @Test
    public void serverListIsNotNull() {
        assertNotNull(defaultConfig.getServerList());
    }

    @Test
    public void defaultProtocols() {
        assertEquals(EnumSet.of(ProtocolTypes.TCP), defaultConfig.getProtocolTypes());
    }

    @Test
    public void publicUniverse() {
        assertEquals(EUniverse.Public, defaultConfig.getUniverse());
    }

    @Test
    public void webAPIAddress() {
        assertEquals("https://api.steampowered.com/", defaultConfig.getWebAPIBaseAddress());
    }

    @Test
    public void noWebApiKey() {
        assertNull(defaultConfig.getWebAPIKey());
    }

    @Test
    public void allowDirectoryFetchConfigured() {
        assertFalse(modifiedConfig.isAllowDirectoryFetch());
    }

    @Test
    public void cellIDConfigured() {
        assertEquals(123, modifiedConfig.getCellID());
    }

    @Test
    public void connectionTimeoutConfigured() {
        assertEquals(60000L, modifiedConfig.getConnectionTimeout());
    }

    @Test
    public void personaStateFlagsConfigured() {
        EnumSet<EClientPersonaStateFlag> expected = EnumSet.of(
                EClientPersonaStateFlag.SourceID
        );
        assertEquals(expected, modifiedConfig.getDefaultPersonaStateFlags());
    }

    @Test
    public void httpClientConfigured() {
        assertNotNull(modifiedConfig.getHttpClient());
        assertEquals(60000L, modifiedConfig.getHttpClient().connectTimeoutMillis());
    }

    @Test
    public void serverListProviderConfigured() {
        assertTrue(modifiedConfig.getServerListProvider() instanceof CustomServerListProvider);
    }

    @Test
    public void serverListIsNotNullConfigured() {
        assertNotNull(modifiedConfig.getServerList());
    }

    @Test
    public void protocolsConfigured() {
        assertEquals(EnumSet.of(ProtocolTypes.WEB_SOCKET, ProtocolTypes.UDP), modifiedConfig.getProtocolTypes());
    }

    @Test
    public void universeConfigured() {
        assertEquals(EUniverse.Internal, modifiedConfig.getUniverse());
    }

    @Test
    public void webAPIAddressConfigured() {
        assertEquals("http://foo.bar.com/api/", modifiedConfig.getWebAPIBaseAddress());
    }

    @Test
    public void webApiKeyConfigured() {
        assertEquals("T0PS3kR1t", modifiedConfig.getWebAPIKey());
    }

    private static class CustomServerListProvider implements IServerListProvider {

        @Override
        public List<ServerRecord> fetchServerList() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateServerList(List<ServerRecord> endpoints) {
            throw new UnsupportedOperationException();
        }
    }
}