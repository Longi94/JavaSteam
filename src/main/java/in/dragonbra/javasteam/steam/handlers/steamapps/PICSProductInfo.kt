package `in`.dragonbra.javasteam.steam.handlers.steamapps

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URI
import java.util.*

/**
 * Represents the information for a single app or package
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class PICSProductInfo : CallbackMsg {

    companion object {
        private val logger = LogManager.getLogger(PICSProductInfo::class.java)
    }

    /**
     * Gets the ID of the app or package.
     */
    val id: Int

    /**
     * Gets the current change number for the app or package.
     */
    val changeNumber: Int

    /**
     * Gets if an access token was required for the request.
     */
    val isMissingToken: Boolean

    /**
     * Gets the hash of the content.
     */
    val shaHash: ByteArray?

    /**
     * Gets the KeyValue info.
     */
    val keyValues: KeyValue

    /**
     * Gets for an app request, returns if only the public information was requested.
     */
    var isOnlyPublic: Boolean = false
        private set

    /**
     * Gets whether to use HTTP to load the KeyValues data.
     */
    var isUseHttp: Boolean = false
        private set

    /**
     * Gets for an app metadata-only request, returns the Uri for HTTP appinfo requests.
     */
    val httpUri: URI?
        get() {
            // We should have all these fields set for the response to a metadata-only request, but guard here just in case.
            if (!hasValidHttpUri) {
                return null
            }

            val shaString = Strings.toHex(shaHash).replace("-", "").lowercase(Locale.getDefault())
            val uriString = String.format("https://%s/appinfo/%d/sha/%s.txt.gz", httpHost, id, shaString)
            return URI.create(uriString)
        }

    private var httpHost: String? = null

    private val hasValidHttpUri: Boolean
        get() = shaHash != null && shaHash.isNotEmpty() && !httpHost.isNullOrEmpty()

    constructor(
        parentResponse: CMsgClientPICSProductInfoResponse.Builder,
        appInfo: CMsgClientPICSProductInfoResponse.AppInfo,
    ) {
        id = appInfo.appid
        changeNumber = appInfo.changeNumber
        isMissingToken = appInfo.missingToken
        shaHash = appInfo.sha.toByteArray()

        keyValues = KeyValue()

        if (appInfo.hasBuffer() && !appInfo.buffer.isEmpty) {
            try {
                // we don't want to read the trailing null byte
                MemoryStream(appInfo.buffer.toByteArray(), 0, appInfo.buffer.size() - 1).use { ms ->
                    keyValues.readAsText(ms)
                }
            } catch (e: IOException) {
                logger.error("failed to read buffer", e)
            }
        }

        isOnlyPublic = appInfo.onlyPublic
        httpHost = parentResponse.httpHost
        isUseHttp = httpUri != null && appInfo.size >= parentResponse.httpMinSize
    }

    constructor(packageInfo: CMsgClientPICSProductInfoResponse.PackageInfo) {
        id = packageInfo.packageid
        changeNumber = packageInfo.changeNumber
        isMissingToken = packageInfo.missingToken
        shaHash = packageInfo.sha.toByteArray()

        keyValues = KeyValue()

        if (packageInfo.hasBuffer()) {
            // we don't want to read the trailing null byte
            try {
                // TODO memory stream like SK?
                BinaryReader(ByteArrayInputStream(packageInfo.buffer.toByteArray())).use { br ->
                    // steamclient checks this value == 1 before it attempts to read the KV from the buffer
                    // see: CPackageInfo::UpdateFromBuffer(CSHA const&,uint,CUtlBuffer &)
                    // todo: (SK) we've apparently ignored this with zero ill effects, but perhaps we want to respect it?
                    br.readInt()
                    keyValues.tryReadAsBinary(br)
                }
            } catch (e: IOException) {
                logger.error("failed to read buffer", e)
            }
        }
    }
}
