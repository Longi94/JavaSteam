package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_GetAppFileChangelist_Response

class AppFileChangeList(response: CCloud_GetAppFileChangelist_Response.Builder) {
    val currentChangeNumber: Long = response.currentChangeNumber
    val files: List<AppFileInfo> = response.filesList.map { AppFileInfo(it) }
    val isOnlyDelta: Boolean = response.isOnlyDelta
    val pathPrefixes: List<String> = response.pathPrefixesList
    val machineNames: List<String> = response.machineNamesList
    val appBuildIDHwm: Long = response.appBuildidHwm
}
