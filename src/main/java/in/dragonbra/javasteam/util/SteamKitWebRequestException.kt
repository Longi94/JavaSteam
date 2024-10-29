package `in`.dragonbra.javasteam.util

import okhttp3.Headers
import okhttp3.Response
import java.lang.Exception

/**
 * Thrown when an HTTP request fails.
 */
class SteamKitWebRequestException(message: String): Exception(message) {
    /**
     * Represents the status code of the HTTP response.
     */
    var statusCode: Int = 0

    /**
     * Represents the collection of HTTP response headers.
     */
    var headers: Headers? = null
        private set

    /**
     * Initializes a new instance of the [SteamKitWebRequestException] class.
     * @param message The message that describes the error.
     * @param response HTTP response message including the status code and data.
     */
    constructor(message: String, response: Response) : this(message) {
        statusCode = response.code
        headers = response.headers
    }
}
