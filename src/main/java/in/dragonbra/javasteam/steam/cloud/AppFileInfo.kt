package `in`.dragonbra.javasteam.steam.cloud

import `in`.dragonbra.javasteam.protobufs.steamclient.Enums.ECloudStoragePersistState
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppFileInfo
import java.util.Date

class AppFileInfo(response: CCloud_AppFileInfo) {
    val filename: String = response.fileName
    val shaFile: ByteArray = response.shaFile.toByteArray()
    val timestamp: Date = Date(response.timeStamp * 1000L)
    val rawFileSize: Int = response.rawFileSize
    val persistState: ECloudStoragePersistState = response.persistState
    val platformsToSync: Int = response.platformsToSync
    val pathPrefixIndex: Int = response.pathPrefixIndex
    val machineNameIndex: Int = response.machineNameIndex
}
