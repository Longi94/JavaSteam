package `in`.dragonbra.javasteam.steam.handlers.steamapps

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.generated.MsgClientGetLegacyGameKey
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGetAppOwnershipTicket
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientCheckAppBetaPassword
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetCDNAuthToken
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetDepotDecryptionKey
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRequestFreeLicense
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSAccessTokenRequest
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSChangesSinceRequest
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoRequest
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.AppOwnershipTicketCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.CDNAuthTokenCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.CheckAppBetaPasswordCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.DepotKeyCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.FreeLicenseCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.GameConnectTokensCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.GuestPassListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LegacyGameKeyCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSChangesCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSTokensCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PurchaseResponseCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.RedeemGuestPassResponseCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.VACStatusCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobMultiple
import `in`.dragonbra.javasteam.types.AsyncJobSingle

/**
 * This handler is used for interacting with apps and packages on the Steam network.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SteamApps : ClientMsgHandler() {

    /**
     * Requests an app ownership ticket for the specified AppID.
     * Results are returned in a [AppOwnershipTicketCallback] callback.
     *
     * @param appId The appid to request the ownership ticket of.
     * @return The Job ID of the request. This can be used to find the appropriate [AppOwnershipTicketCallback].
     */
    fun getAppOwnershipTicket(appId: Int): AsyncJobSingle<AppOwnershipTicketCallback> {
        val request = ClientMsgProtobuf<CMsgClientGetAppOwnershipTicket.Builder>(
            CMsgClientGetAppOwnershipTicket::class.java,
            EMsg.ClientGetAppOwnershipTicket
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = appId
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request the depot decryption key for a specified DepotID.
     * Results are returned in a [DepotKeyCallback] callback.
     *
     * @param depotId The DepotID to request a decryption key for.
     * @param appId   The AppID parent of the DepotID.
     * @return The Job ID of the request. This can be used to find the appropriate [DepotKeyCallback].
     */
    fun getDepotDecryptionKey(depotId: Int, appId: Int): AsyncJobSingle<DepotKeyCallback> {
        val request = ClientMsgProtobuf<CMsgClientGetDepotDecryptionKey.Builder>(
            CMsgClientGetDepotDecryptionKey::class.java,
            EMsg.ClientGetDepotDecryptionKey
        ).apply {
            sourceJobID = client.getNextJobID()

            body.depotId = depotId
            body.appId = appId
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request PICS access tokens for an app or package.
     * Results are returned in a [PICSTokensCallback] callback.
     *
     * @param app      App id to request access token for.
     * @param package Package id to request access token for.
     * @return The Job ID of the request. This can be used to find the appropriate [PICSTokensCallback].
     */
    @JvmOverloads
    fun picsGetAccessTokens(app: Int? = null, `package`: Int? = null): AsyncJobSingle<PICSTokensCallback> {
        val apps = listOfNotNull(app)
        val packages = listOfNotNull(`package`)

        return picsGetAccessTokens(apps, packages)
    }

    /**
     * Request PICS access tokens for a list of app ids and package ids
     * Results are returned in a [PICSTokensCallback] callback.
     *
     * @param appIds     List of app ids to request access tokens for.
     * @param packageIds List of package ids to request access tokens for.
     * @return The Job ID of the request. This can be used to find the appropriate [PICSTokensCallback].
     */
    fun picsGetAccessTokens(appIds: Iterable<Int>, packageIds: Iterable<Int>): AsyncJobSingle<PICSTokensCallback> {
        val request = ClientMsgProtobuf<CMsgClientPICSAccessTokenRequest.Builder>(
            CMsgClientPICSAccessTokenRequest::class.java,
            EMsg.ClientPICSAccessTokenRequest
        ).apply {
            sourceJobID = client.getNextJobID()

            body.addAllAppids(appIds)
            body.addAllPackageids(packageIds)
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request changes for apps and packages since a given change number
     * Results are returned in a [PICSChangesCallback] callback.
     *
     * @param lastChangeNumber      Last change number seen.
     * @param sendAppChangeList     Whether to send app changes.
     * @param sendPackageChangelist Whether to send package changes.
     * @return The Job ID of the request. This can be used to find the appropriate [PICSChangesCallback].
     */
    @JvmOverloads
    fun picsGetChangesSince(
        lastChangeNumber: Int = 0,
        sendAppChangeList: Boolean = true,
        sendPackageChangelist: Boolean = false,
    ): AsyncJobSingle<PICSChangesCallback> {
        val request = ClientMsgProtobuf<CMsgClientPICSChangesSinceRequest.Builder>(
            CMsgClientPICSChangesSinceRequest::class.java,
            EMsg.ClientPICSChangesSinceRequest
        ).apply {
            sourceJobID = client.getNextJobID()

            body.sinceChangeNumber = lastChangeNumber
            body.sendAppInfoChanges = sendAppChangeList
            body.sendPackageInfoChanges = sendPackageChangelist
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request product information for an app or package
     * Results are returned in a [PICSProductInfoCallback] callback.
     *
     * @param app          App id requested.
     * @param package     Package id requested.
     * @param metaDataOnly Whether to send only meta-data.
     * @return The Job ID of the request. This can be used to find the appropriate [PICSProductInfoCallback].
     */
    @JvmOverloads
    fun picsGetProductInfo(
        app: PICSRequest? = null,
        `package`: PICSRequest? = null,
        metaDataOnly: Boolean = false,
    ): AsyncJobMultiple<PICSProductInfoCallback> {
        val apps = listOfNotNull(app)
        val packages = listOfNotNull(`package`)

        return picsGetProductInfo(apps, packages, metaDataOnly)
    }

    /**
     * Request product information for a list of apps or packages
     * Results are returned in a [PICSProductInfoCallback] callback.
     *
     * @param apps         List of [PICSRequest] requests for apps.
     * @param packages     List of [PICSRequest] requests for packages.
     * @param metaDataOnly Whether to send only metadata.
     * @return The Job ID of the request. This can be used to find the appropriate [PICSProductInfoCallback].
     */
    @JvmOverloads
    fun picsGetProductInfo(
        apps: Iterable<PICSRequest>,
        packages: Iterable<PICSRequest>,
        metaDataOnly: Boolean = false,
    ): AsyncJobMultiple<PICSProductInfoCallback> {
        val request = ClientMsgProtobuf<CMsgClientPICSProductInfoRequest.Builder>(
            CMsgClientPICSProductInfoRequest::class.java,
            EMsg.ClientPICSProductInfoRequest
        ).apply {
            sourceJobID = client.getNextJobID()

            apps.forEach { appRequest ->
                val appInfo = CMsgClientPICSProductInfoRequest.AppInfo.newBuilder().apply {
                    accessToken = appRequest.accessToken
                    appid = appRequest.id
                    onlyPublicObsolete = false
                }

                body.addApps(appInfo)
            }

            packages.forEach { packageRequest ->
                val packageInfo = CMsgClientPICSProductInfoRequest.PackageInfo.newBuilder().apply {
                    accessToken = packageRequest.accessToken
                    packageid = packageRequest.id
                }

                body.addPackages(packageInfo)
            }

            body.metaDataOnly = metaDataOnly
        }

        client.send(request)

        return AsyncJobMultiple(client, request.sourceJobID) { cb: PICSProductInfoCallback -> !cb.isResponsePending }
    }

    /**
     * Request product information for an app or package
     * Results are returned in a [CDNAuthTokenCallback] callback.
     *
     * @param app      App id requested.
     * @param depot    Depot id requested.
     * @param hostName CDN host name being requested.
     * @return The Job ID of the request. This can be used to find the appropriate [CDNAuthTokenCallback].
     */
    fun getCDNAuthToken(app: Int, depot: Int, hostName: String): AsyncJobSingle<CDNAuthTokenCallback> {
        val request = ClientMsgProtobuf<CMsgClientGetCDNAuthToken.Builder>(
            CMsgClientGetCDNAuthToken::class.java,
            EMsg.ClientGetCDNAuthToken
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = app
            body.depotId = depot
            body.hostName = hostName
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request a free license for given appid, can be used for free on demand apps
     * Results are returned in a [FreeLicenseCallback] callback.
     *
     * @param app The app to request a free license for.
     * @return The Job ID of the request. This can be used to find the appropriate [FreeLicenseCallback].
     */
    fun requestFreeLicense(app: Int): AsyncJobSingle<FreeLicenseCallback> = requestFreeLicense(listOf(app))

    /**
     * Request a free license for given appids, can be used for free on demand apps
     * Results are returned in a [FreeLicenseCallback] callback.
     *
     * @param apps The apps to request a free license for.
     * @return The Job ID of the request. This can be used to find the appropriate [FreeLicenseCallback].
     */
    fun requestFreeLicense(apps: Iterable<Int>): AsyncJobSingle<FreeLicenseCallback> {
        val request = ClientMsgProtobuf<CMsgClientRequestFreeLicense.Builder>(
            CMsgClientRequestFreeLicense::class.java,
            EMsg.ClientRequestFreeLicense
        ).apply {
            sourceJobID = client.getNextJobID()

            body.addAllAppids(apps)
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Submit a beta password for a given app to retrieve any betas and their encryption keys.
     * Results are returned in a [CheckAppBetaPasswordCallback] callback.
     *
     * @param app      App id requested.
     * @param password Password to check.
     * @return The Job ID of the request. This can be used to find the appropriate [CheckAppBetaPasswordCallback].
     */
    fun checkAppBetaPassword(app: Int, password: String): AsyncJobSingle<CheckAppBetaPasswordCallback> {
        val request = ClientMsgProtobuf<CMsgClientCheckAppBetaPassword.Builder>(
            CMsgClientCheckAppBetaPassword::class.java,
            EMsg.ClientCheckAppBetaPassword
        ).apply {
            sourceJobID = client.getNextJobID()

            body.appId = app
            body.betapassword = password
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Request the legacy CD game keys for the requested appid.
     *
     * @param appId The AppID to request game keys for.
     * @return The Job ID of the request. This can be used to find the appropriate [LegacyGameKeyCallback]
     */
    fun getLegacyGameKey(appId: Int): AsyncJobSingle<LegacyGameKeyCallback> {
        val request = ClientMsg(MsgClientGetLegacyGameKey::class.java).apply {
            sourceJobID = (client.getNextJobID())
            body.appId = appId
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientLicenseList -> LicenseListCallback(packetMsg)
            EMsg.ClientRequestFreeLicenseResponse -> FreeLicenseCallback(packetMsg)
            EMsg.ClientPurchaseResponse -> PurchaseResponseCallback(packetMsg)
            EMsg.ClientRedeemGuestPassResponse -> RedeemGuestPassResponseCallback(packetMsg)
            EMsg.ClientGameConnectTokens -> GameConnectTokensCallback(packetMsg)
            EMsg.ClientVACBanStatus -> VACStatusCallback(packetMsg)
            EMsg.ClientGetAppOwnershipTicketResponse -> AppOwnershipTicketCallback(packetMsg)
            EMsg.ClientGetDepotDecryptionKeyResponse -> DepotKeyCallback(packetMsg)
            EMsg.ClientGetLegacyGameKeyResponse -> LegacyGameKeyCallback(packetMsg)
            EMsg.ClientPICSAccessTokenResponse -> PICSTokensCallback(packetMsg)
            EMsg.ClientPICSChangesSinceResponse -> PICSChangesCallback(packetMsg)
            EMsg.ClientPICSProductInfoResponse -> PICSProductInfoCallback(packetMsg)
            EMsg.ClientUpdateGuestPassesList -> GuestPassListCallback(packetMsg)
            EMsg.ClientGetCDNAuthTokenResponse -> CDNAuthTokenCallback(packetMsg)
            EMsg.ClientCheckAppBetaPasswordResponse -> CheckAppBetaPasswordCallback(packetMsg)
            else -> null
        }
    }
}
