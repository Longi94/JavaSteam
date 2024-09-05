package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.MemoryServerListProvider;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

/**
 * Tests SteamConfiguration with default values
 */
public class SteamConfigurationDefaultTest {

    private final SteamConfiguration configuration = SteamConfiguration.createDefault();

    @Test
    public void allowsDirectoryFetch() {
        Assertions.assertTrue(configuration.isAllowDirectoryFetch());
    }

    @Test
    public void cellIDIsZero() {
        Assertions.assertEquals(0, configuration.getCellID());
    }

    @Test
    public void connectionTimeoutIsFiveSeconds() {
        Assertions.assertEquals(5000L, configuration.getConnectionTimeout());
    }

    @Test
    public void defaultPersonaStateFlags() {
        var expected = EnumSet.of(
                EClientPersonaStateFlag.PlayerName,
                EClientPersonaStateFlag.Presence,
                EClientPersonaStateFlag.SourceID,
                EClientPersonaStateFlag.GameExtraInfo,
                EClientPersonaStateFlag.LastSeen);

        Assertions.assertEquals(expected, configuration.getDefaultPersonaStateFlags());
    }

    @Test
    public void defaultHttpClientFactory() {
        var client = configuration.getHttpClient();
        Assertions.assertNotNull(client);
        Assertions.assertInstanceOf(OkHttpClient.class, client);
    }

    // @Test
    // public void defaultMachineInfoProvider() {
    // }

    @Test
    public void serverListProviderIsNothingFancy() {
        Assertions.assertInstanceOf(MemoryServerListProvider.class, configuration.getServerListProvider());
    }

    @Test
    public void serverListIsNotNull() {
        Assertions.assertNotNull(configuration.getServerList());
    }

    @Test
    public void defaultProtocols() {
        Assertions.assertEquals(EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.WEB_SOCKET), configuration.getProtocolTypes());
    }

    @Test
    public void publicUniverse() {
        Assertions.assertEquals(EUniverse.Public, configuration.getUniverse());
    }

    @Test
    public void webAPIAddress() {
        Assertions.assertEquals("https://api.steampowered.com/", configuration.getWebAPIBaseAddress());
    }

    @Test
    public void noWebAPIKey() {
        Assertions.assertNull(configuration.getWebAPIKey());
    }
}
