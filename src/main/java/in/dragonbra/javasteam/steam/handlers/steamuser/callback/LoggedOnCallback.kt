package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import com.google.protobuf.InvalidProtocolBufferException
import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EAccountFlags
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.generated.MsgClientLogOnResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalObjects.ParentalSettings
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import `in`.dragonbra.javasteam.util.log.LogManager
import java.net.InetAddress
import java.util.*

/**
 * This callback is returned in response to an attempt to log on to the Steam3 network through [SteamUser].
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class LoggedOnCallback : CallbackMsg {

    companion object {
        private val logger = LogManager.getLogger<LoggedOnCallback>()
    }

    /**
     * Gets the result of the logon.
     */
    var result: EResult = EResult.Invalid
        private set

    /**
     * Gets the extended result of the logon.
     */
    var extendedResult: EResult? = null

    /**
     * Gets the out of game secs per heartbeat value.
     * This is used internally to initialize heartbeating.
     */
    var outOfGameSecsPerHeartbeat: Int = 0
        private set

    /**
     * Gets the in game secs per heartbeat value.
     * This is used internally to initialize heartbeating.
     */
    var inGameSecsPerHeartbeat: Int = 0
        private set

    /**
     * Gets or sets the public IP of the client
     */
    var publicIP: InetAddress? = null
        private set

    /**
     * Gets the Steam3 server time.
     */
    var serverTime: Date? = null
        private set

    /**
     * Gets the account flags assigned by the server.
     */
    var accountFlags: EnumSet<EAccountFlags>? = null

    /**
     * Gets the client steam ID.
     */
    var clientSteamID: SteamID? = null

    /**
     * Gets the email domain.
     */
    var emailDomain: String? = null
        private set

    /**
     * Gets the Steam2 CellID.
     */
    var cellID: Int = 0
        private set

    /**
     * Gets the Steam2 CellID ping threshold.
     */
    var cellIDPingThreshold: Int = 0
        private set

    /**
     * Gets the Steam2 ticket.
     * This is used for authenticated content downloads in Steam2.
     * This field will only be set when [LogOnDetails.requestSteam2Ticket] has been set to true.
     */
    var steam2Ticket: ByteArray? = null
        private set

    /**
     * Gets the IP country code.
     */
    var ipCountryCode: String? = null
        private set

    /**
     * Gets the vanity URL.
     */
    var vanityURL: String? = null
        private set

    /**
     * Gets the threshold for login failures before Steam wants the client to migrate to a new CM.
     */
    var numLoginFailuresToMigrate: Int = 0
        private set

    /**
     * Gets the threshold for disconnects before Steam wants the client to migrate to a new CM.
     */
    var numDisconnectsToMigrate: Int = 0
        private set

    /**
     * Gets the Steam parental settings.
     */
    var parentalSettings: ParentalSettings? = null

    /**
     * Gets the id of the family group a user is joined in.
     */
    var familyGroupId: Long = 0L
        private set

    /**
     * Gets the client instance ID.
     * This is used for P2P content sharing operations.
     */
    var clientInstanceId: Long = 0L
        private set

    constructor(packetMsg: IPacketMsg) {
        if (!packetMsg.isProto) {
            handleNonProtoLogon(packetMsg)
            return
        }

        val loginResp = ClientMsgProtobuf<CMsgClientLogonResponse.Builder>(
            CMsgClientLogonResponse::class.java,
            packetMsg
        )
        val resp = loginResp.body

        result = EResult.from(resp.eresult)
        extendedResult = EResult.from(resp.eresultExtended)

        outOfGameSecsPerHeartbeat = resp.legacyOutOfGameHeartbeatSeconds
        inGameSecsPerHeartbeat = resp.heartbeatSeconds

        publicIP = NetHelpers.getIPAddress(resp.publicIp)

        serverTime = Date(resp.rtime32ServerTime * 1000L)

        accountFlags = EAccountFlags.from(resp.accountFlags)

        clientSteamID = SteamID(resp.clientSuppliedSteamid)

        emailDomain = resp.emailDomain

        cellID = resp.cellId
        cellIDPingThreshold = resp.cellIdPingThreshold

        steam2Ticket = resp.steam2Ticket.toByteArray()

        ipCountryCode = resp.ipCountryCode

        vanityURL = resp.vanityUrl

        numLoginFailuresToMigrate = resp.countLoginfailuresToMigrate
        numDisconnectsToMigrate = resp.countDisconnectsToMigrate

        if (resp.parentalSettings != null) {
            try {
                parentalSettings = ParentalSettings.parseFrom(resp.parentalSettings)
            } catch (e: InvalidProtocolBufferException) {
                logger.error("Failed to parse parental settings", e)
            }
        }

        familyGroupId = resp.familyGroupId

        clientInstanceId = resp.clientInstanceId
    }

    constructor(result: EResult) {
        this.result = result
    }

    private fun handleNonProtoLogon(packetMsg: IPacketMsg) {
        val loginResp = ClientMsg(MsgClientLogOnResponse::class.java, packetMsg)
        val resp = loginResp.body

        result = resp.result

        outOfGameSecsPerHeartbeat = resp.outOfGameHeartbeatRateSec
        inGameSecsPerHeartbeat = resp.inGameHeartbeatRateSec

        publicIP = NetHelpers.getIPAddress(resp.ipPublic)

        serverTime = Date(resp.serverRealTime * 1000L)

        clientSteamID = resp.clientSuppliedSteamId
    }
}
