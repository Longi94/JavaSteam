package `in`.dragonbra.javasteam.steam.steamclient.configuration

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.networking.steam3.IConnectionFactory
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.contentdownloader.IManifestProvider
import `in`.dragonbra.javasteam.steam.contentdownloader.MemoryManifestProvider
import `in`.dragonbra.javasteam.steam.discovery.IServerListProvider
import `in`.dragonbra.javasteam.steam.discovery.MemoryServerListProvider
import `in`.dragonbra.javasteam.steam.webapi.WebAPI
import okhttp3.OkHttpClient
import java.util.*

/**
 * @author lngtr
 * @since 2018-02-20
 */
class SteamConfigurationBuilder : ISteamConfigurationBuilder {

    private val state: SteamConfigurationState = createDefaultState()

    fun build(): SteamConfiguration = SteamConfiguration(state)

    override fun withConnectionFactory(connectionFactory: IConnectionFactory): ISteamConfigurationBuilder {
        state.connectionFactory = connectionFactory
        return this
    }

    override fun withCellID(cellID: Int): ISteamConfigurationBuilder {
        state.cellID = cellID
        return this
    }

    override fun withConnectionTimeout(connectionTimeout: Long): ISteamConfigurationBuilder {
        state.connectionTimeout = connectionTimeout
        return this
    }

    override fun withHttpClient(httpClient: OkHttpClient): ISteamConfigurationBuilder {
        state.httpClient = httpClient
        return this
    }

    override fun withDefaultPersonaStateFlags(personaStateFlags: EnumSet<EClientPersonaStateFlag>): ISteamConfigurationBuilder {
        state.defaultPersonaStateFlags = personaStateFlags
        return this
    }

    override fun withDefaultPersonaStateFlags(personaStateFlags: EClientPersonaStateFlag): ISteamConfigurationBuilder {
        state.defaultPersonaStateFlags = EnumSet.of(personaStateFlags)
        return this
    }

    override fun withDirectoryFetch(allowDirectoryFetch: Boolean): ISteamConfigurationBuilder {
        state.isAllowDirectoryFetch = allowDirectoryFetch
        return this
    }

    override fun withProtocolTypes(protocolTypes: EnumSet<ProtocolTypes>): ISteamConfigurationBuilder {
        state.protocolTypes = protocolTypes
        return this
    }

    override fun withProtocolTypes(protocolTypes: ProtocolTypes): ISteamConfigurationBuilder {
        state.protocolTypes = EnumSet.of(protocolTypes)
        return this
    }

    override fun withServerListProvider(provider: IServerListProvider): ISteamConfigurationBuilder {
        state.serverListProvider = provider
        return this
    }

    override fun withManifestProvider(provider: IManifestProvider): ISteamConfigurationBuilder {
        state.depotManifestProvider = provider
        return this
    }

    override fun withUniverse(universe: EUniverse): ISteamConfigurationBuilder {
        state.universe = universe
        return this
    }

    override fun withWebAPIBaseAddress(baseAddress: String): ISteamConfigurationBuilder {
        state.webAPIBaseAddress = baseAddress
        return this
    }

    override fun withWebAPIKey(webApiKey: String): ISteamConfigurationBuilder {
        state.webAPIKey = webApiKey
        return this
    }

    companion object {
        @JvmStatic
        fun createDefaultState(): SteamConfigurationState = SteamConfigurationState(
            connectionFactory = IConnectionFactory.DEFAULT,
            isAllowDirectoryFetch = true,
            connectionTimeout = 5000L,
            defaultPersonaStateFlags = EnumSet.of(
                EClientPersonaStateFlag.PlayerName,
                EClientPersonaStateFlag.Presence,
                EClientPersonaStateFlag.SourceID,
                EClientPersonaStateFlag.GameExtraInfo,
                EClientPersonaStateFlag.LastSeen
            ),
            httpClient = OkHttpClient(),
            protocolTypes = EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.WEB_SOCKET),
            serverListProvider = MemoryServerListProvider(),
            depotManifestProvider = MemoryManifestProvider(),
            universe = EUniverse.Public,
            webAPIBaseAddress = WebAPI.DEFAULT_BASE_ADDRESS,
            cellID = 0,
            webAPIKey = null,
        )
    }
}
