package `in`.dragonbra.javasteam.steam.steamclient.configuration

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EUniverse
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.steam.contentdownloader.IManifestProvider
import `in`.dragonbra.javasteam.steam.discovery.IServerListProvider
import okhttp3.OkHttpClient
import java.net.Proxy
import java.util.EnumSet

/**
 * @author lngtr
 * @since 2018-02-20
 */
data class SteamConfigurationState(
    var isAllowDirectoryFetch: Boolean,
    var cellID: Int,
    var connectionTimeout: Long,
    var defaultPersonaStateFlags: EnumSet<EClientPersonaStateFlag>,
    var httpClient: OkHttpClient,
    var protocolTypes: EnumSet<ProtocolTypes>,
    var serverListProvider: IServerListProvider,
    var depotManifestProvider: IManifestProvider,
    var universe: EUniverse,
    var webAPIBaseAddress: String,
    var webAPIKey: String?,
    var proxy: Proxy?,
)
