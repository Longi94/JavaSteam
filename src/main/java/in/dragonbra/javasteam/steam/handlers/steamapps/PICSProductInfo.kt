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
        private val logger = LogManager.getLogger<PICSProductInfo>()

        /**
         * Reads a [KeyValue] from a non-empty text buffer, which may end with a null byte.
         * @return the parsed [KeyValue]. If the buffer cannot be read, [onError] is called and an empty one is returned.
         */
        internal fun readTextBuffer(buffer: ByteArray, onError: (Exception) -> Unit): KeyValue {
            val textLength = if (buffer.last() == 0.toByte()) buffer.size - 1 else buffer.size

            return try {
                KeyValue().apply {
                    MemoryStream(buffer, 0, textLength).use { ms -> readAsText(ms) }
                }
            } catch (e: Exception) {
                onError(e)
                KeyValue()
            }
        }
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

            val shaHash = shaHash ?: return null
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

        keyValues = if (appInfo.hasBuffer() && !appInfo.buffer.isEmpty) {
            readTextBuffer(appInfo.buffer.toByteArray()) { error ->
                logger.error("Failed to load text buffer for app $id, change number $changeNumber", error)
            }
        } else {
            if (!parentResponse.metaDataOnly && !appInfo.missingToken) {
                logger.debug("Empty buffer for app $id, change number $changeNumber")
            }
            KeyValue()
        }

        isOnlyPublic = appInfo.onlyPublic
        httpHost = parentResponse.httpHost

        // a minimum size of zero means HTTP is not available for this response
        val httpMinSize = parentResponse.httpMinSize
        val httpAvailable = parentResponse.metaDataOnly && httpMinSize > 0
        isUseHttp = httpAvailable && hasValidHttpUri && appInfo.size >= httpMinSize
    }

    constructor(
        parentResponse: CMsgClientPICSProductInfoResponse.Builder,
        packageInfo: CMsgClientPICSProductInfoResponse.PackageInfo,
    ) {
        id = packageInfo.packageid
        changeNumber = packageInfo.changeNumber
        isMissingToken = packageInfo.missingToken
        shaHash = packageInfo.sha.toByteArray()

        keyValues = KeyValue()

        if (packageInfo.hasBuffer() && packageInfo.buffer.size() >= Int.SIZE_BYTES) {
            try {
                BinaryReader(ByteArrayInputStream(packageInfo.buffer.toByteArray())).use { br ->
                    // steamclient checks this value == 1 before it attempts to read the KV from the buffer
                    // see: CPackageInfo::UpdateFromBuffer(CSHA const&,uint,CUtlBuffer &)
                    val format = br.readInt()

                    if (format != 1) {
                        logger.debug(
                            "Unknown package info format $format for package $id, size ${packageInfo.buffer.size()}"
                        )
                    } else if (!keyValues.tryReadAsBinary(br)) {
                        logger.debug("Failed to parse binary buffer for package $id, change number $changeNumber")
                    }
                }
            } catch (e: IOException) {
                logger.error("Failed to parse binary buffer for package $id, change number $changeNumber", e)
            }
        } else if (!parentResponse.metaDataOnly && !packageInfo.missingToken) {
            logger.debug("Empty or truncated buffer for package $id, change number $changeNumber")
        }
    }
}
