package `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendProfileInfoResponse
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.SteamID
import java.util.*

/**
 * This callback is fired in response to requesting profile info for a user.
 */
@Suppress("unused")
class ProfileInfoCallback : CallbackMsg {

    /**
     * Gets the result of requesting profile info.
     */
    val result: EResult

    /**
     * Gets the [SteamID] this info belongs to.
     */
    val steamID: SteamID

    /**
     * Gets the time this account was created.
     */
    val timeCreated: Date

    /**
     * Gets the real name.
     */
    val realName: String

    /**
     * Gets the name of the city.
     */
    val cityName: String

    /**
     * Gets the name of the state.
     */
    val stateName: String

    /**
     * Gets the name of the country.
     */
    val countryName: String

    /**
     * Gets the headline.
     */
    val headline: String

    /**
     * Gets the summary.
     */
    val summary: String

    constructor(packetMsg: IPacketMsg) {
        val responseMsg = ClientMsgProtobuf<CMsgClientFriendProfileInfoResponse.Builder>(
            CMsgClientFriendProfileInfoResponse::class.java,
            packetMsg
        )
        val response = responseMsg.body

        jobID = responseMsg.targetJobID

        result = EResult.from(response.eresult)

        steamID = SteamID(response.steamidFriend)

        timeCreated = Date(response.timeCreated * 1000L)

        realName = response.realName

        cityName = response.cityName
        stateName = response.stateName
        countryName = response.countryName

        headline = response.headline

        summary = response.summary
    }

    constructor(
        result: EResult,
        steamID: SteamID,
        timeCreated: Date,
        realName: String,
        cityName: String,
        stateName: String,
        countryName: String,
        headline: String,
        summary: String,
    ) {
        this.result = result
        this.steamID = steamID
        this.timeCreated = timeCreated
        this.realName = realName
        this.cityName = cityName
        this.stateName = stateName
        this.countryName = countryName
        this.headline = headline
        this.summary = summary
    }
}
