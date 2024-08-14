package `in`.dragonbra.javasteam.steam.handlers.steamapps

/**
 * Represents a PICS request used for [SteamApps.picsGetProductInfo]
 *
 * @constructor Instantiate a PICS product info request for a given app and/or package id and an access token
 * @param id App or package ID PICS access token
 * @param accessToken
 *
 * @property id Gets or sets the ID of the app or package being requested
 * @property accessToken Gets or sets the access token associated with the request
 */
class PICSRequest @JvmOverloads constructor(var id: Int = 0, var accessToken: Long = 0L)
