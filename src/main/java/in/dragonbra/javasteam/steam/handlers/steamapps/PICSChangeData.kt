package `in`.dragonbra.javasteam.steam.handlers.steamapps

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceResponse

/**
 * Holds the change data for a single app or package
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class PICSChangeData {

    /**
     * Gets the app or package ID this change data represents
     */
    val id: Int

    /**
     * Gets the current change number of this app
     */
    val changeNumber: Int

    /**
     * Gets signals if an access token is needed for this request
     */
    val isNeedsToken: Boolean

    constructor(change: CMsgClientPICSChangesSinceResponse.AppChange) {
        id = change.appid
        changeNumber = change.changeNumber
        isNeedsToken = change.needsToken
    }

    constructor(change: CMsgClientPICSChangesSinceResponse.PackageChange) {
        id = change.packageid
        changeNumber = change.changeNumber
        isNeedsToken = change.needsToken
    }
}
