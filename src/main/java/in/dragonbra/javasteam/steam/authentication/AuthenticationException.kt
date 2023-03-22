package `in`.dragonbra.javasteam.steam.authentication

import `in`.dragonbra.javasteam.enums.EResult

/**
 * Thrown when [SteamAuthentication] fails to authenticate.
 */
@Suppress("unused")
class AuthenticationException : Exception {

    /**
     *  The result of the authentication request.
     */
    var result: EResult? = null
        private set

    /**
     * Initializes a new instance of the [AuthenticationException] class.
     */
    constructor() : super()

    /**
     * Initializes a new instance of the [AuthenticationException] class.
     *
     * @param message The message that describes the error.
     * @param result  The result code that describes the error.
     */
    constructor(message: String, result: EResult) : super("$message with result $result") {
        this.result = result
    }
}