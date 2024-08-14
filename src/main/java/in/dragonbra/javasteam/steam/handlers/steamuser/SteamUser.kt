package `in`.dragonbra.javasteam.steam.handlers.steamuser

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EAccountType
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.enums.EUIMode
import `in`.dragonbra.javasteam.generated.MsgClientLogon
import `in`.dragonbra.javasteam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgIPAddress
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogOff
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogon
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.AccountInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.EmailAddrInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.MarketingMessageCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.PlayingSessionStateCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.SessionTokenCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.VanityURLChangedCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.WalletInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.WebAPIUserNonceCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.HardwareUtils
import `in`.dragonbra.javasteam.util.NetHelpers

/**
 * This handler handles all user log on/log off related actions and callbacks.
 */
class SteamUser : ClientMsgHandler() {

    val steamID: SteamID
        get() = client.steamID

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        // ignore messages that we don't have a handler function for
        val callback = getCallback(packetMsg) ?: return

        client.postCallback(callback)
    }

    /**
     * Logs the client into the Steam3 network.
     * The client should already have been connected at this point.
     * Results are returned in a [LoggedOnCallback].
     *
     * @param details The details to use for logging on.
     */
    fun logOn(details: LogOnDetails) {
        if (details.username.isEmpty() || (details.password.isNullOrEmpty() && details.accessToken.isNullOrEmpty())) {
            throw IllegalArgumentException("LogOn requires a username and password or access token to be set in 'details'.")
        }

        if (!client.isConnected) {
            client.postCallback(LoggedOnCallback(EResult.NoConnection))
            return
        }

        val logon = ClientMsgProtobuf<CMsgClientLogon.Builder>(CMsgClientLogon::class.java, EMsg.ClientLogon)

        val steamID = SteamID(details.accountID, details.accountInstance, client.universe, EAccountType.Individual)

        if (details.loginID != null) {
            // TODO: Support IPv6 login ids?
            CMsgIPAddress.newBuilder().apply {
                v4 = details.loginID!!
            }.build().also(logon.body::setObfuscatedPrivateIp)
        } else {
            CMsgIPAddress.newBuilder().apply {
                v4 = NetHelpers.getIPAddress(client.localIP) xor MsgClientLogon.ObfuscationMask
            }.build().also(logon.body::setObfuscatedPrivateIp)
        }

        // Legacy field, Steam client still sets it
        if (logon.body.obfuscatedPrivateIp.hasV4()) {
            logon.body.setDeprecatedObfustucatedPrivateIp(logon.body.obfuscatedPrivateIp.getV4())
        }

        logon.protoHeader.setClientSessionid(0)
        logon.protoHeader.setSteamid(steamID.convertToUInt64())

        logon.body.setAccountName(details.username)
        details.password?.let { logon.body.setPassword(it) }
        logon.body.setShouldRememberPassword(details.shouldRememberPassword)

        logon.body.setProtocolVersion(MsgClientLogon.CurrentProtocol)
        logon.body.setClientOsType(details.clientOSType.code())
        logon.body.setClientLanguage(details.clientLanguage)
        logon.body.setCellId(details.cellID ?: client.configuration.cellID)

        logon.body.setSteam2TicketRequest(details.requestSteam2Ticket)

        // we're now using the latest steamclient package version, this is required to get a proper sentry file for steam guard
        logon.body.setClientPackageVersion(1771) // todo: determine if this is still required
        logon.body.setSupportsRateLimitResponse(true)
        logon.body.setMachineName(details.machineName)
        val machineId = ByteString.copyFrom(HardwareUtils.getMachineID())
        logon.body.setMachineId(machineId)

        if (details.chatMode != ChatMode.DEFAULT) {
            logon.body.setChatMode(details.chatMode.mode)
        }

        if (details.uiMode != EUIMode.Unknown) {
            logon.body.setUiMode(details.uiMode.code())
        }

        if (details.isSteamDeck) {
            logon.body.setIsSteamDeck(true)
        }

        // steam guard
        details.authCode?.let(logon.body::setAuthCode)
        details.twoFactorCode?.let(logon.body::setTwoFactorCode)

        details.accessToken?.let(logon.body::setAccessToken)

        client.send(logon)
    }

    /**
     * Logs the client into the Steam3 network as an anonymous user.
     * The client should already have been connected at this point.
     * Results are returned in a [LoggedOnCallback].
     *
     * @param details The details to use for logging on.
     */
    @JvmOverloads
    fun logOnAnonymous(details: AnonymousLogOnDetails = AnonymousLogOnDetails()) {
        if (!client.isConnected) {
            client.postCallback(LoggedOnCallback(EResult.NoConnection))
            return
        }

        val logon = ClientMsgProtobuf<CMsgClientLogon.Builder>(CMsgClientLogon::class.java, EMsg.ClientLogon)

        val auId = SteamID(0, 0, client.universe, EAccountType.AnonUser)

        logon.protoHeader.setClientSessionid(0)
        logon.protoHeader.setSteamid(auId.convertToUInt64())

        logon.body.setProtocolVersion(MsgClientLogon.CurrentProtocol)
        logon.body.setClientOsType(details.clientOSType.code())
        logon.body.setClientLanguage(details.clientLanguage)
        logon.body.setCellId(details.cellID ?: client.configuration.cellID)

        val machineId = ByteString.copyFrom(HardwareUtils.getMachineID())
        logon.body.setMachineId(machineId)

        client.send(logon)
    }

    /**
     * Informs the Steam servers that this client wishes to log off from the network.
     * The Steam server will disconnect the client, and a [DisconnectedCallback] will be posted.
     */
    fun logOff() {
        isExpectDisconnection = true

        val logOff = ClientMsgProtobuf<CMsgClientLogOff.Builder>(CMsgClientLogOff::class.java, EMsg.ClientLogOff)
        client.send(logOff)

        // TODO: 2018-02-28 it seems like the socket is not closed after getting logged of or I am doing something horribly wrong, let's disconnect here
        client.disconnect()
    }

    companion object {
        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientLogOnResponse -> LoggedOnCallback(packetMsg)
            EMsg.ClientLoggedOff -> LoggedOffCallback(packetMsg)
            EMsg.ClientSessionToken -> SessionTokenCallback(packetMsg)
            EMsg.ClientAccountInfo -> AccountInfoCallback(packetMsg)
            EMsg.ClientEmailAddrInfo -> EmailAddrInfoCallback(packetMsg)
            EMsg.ClientWalletInfoUpdate -> WalletInfoCallback(packetMsg)
            EMsg.ClientRequestWebAPIAuthenticateUserNonceResponse -> WebAPIUserNonceCallback(packetMsg)
            EMsg.ClientVanityURLChangedNotification -> VanityURLChangedCallback(packetMsg)
            EMsg.ClientMarketingMessageUpdate2 -> MarketingMessageCallback(packetMsg)
            EMsg.ClientPlayingSessionState -> PlayingSessionStateCallback(packetMsg)
            else -> null
        }
    }
}
