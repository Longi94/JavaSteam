package `in`.dragonbra.javasteamsamples._1logon

import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.authentication.*
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.log.DefaultLogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * @author lossy
 * @since 2023-11-06
 */
class SampleWebCookieKt(private val user: String, private val pass: String) : Runnable {

    private lateinit var steamClient: SteamClient

    private lateinit var unifiedMessages: SteamUnifiedMessages

    private lateinit var auth: SteamAuthentication

    private lateinit var manager: CallbackManager

    private lateinit var steamUser: SteamUser

    private var isRunning = false

    private var accessToken: String = ""

    private var refreshToken: String = ""

    override fun run() {

        // create our steamclient instance
        steamClient = SteamClient()

        // create the callback manager which will route callbacks to function calls
        manager = CallbackManager(steamClient)

        // get the steam unified messages handler, which is used for sending and receiving responses from the unified service api
        unifiedMessages = steamClient.getHandler(SteamUnifiedMessages::class.java)

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUser::class.java)

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback::class.java, ::onConnected)
        manager.subscribe(DisconnectedCallback::class.java, ::onDisconnected)
        manager.subscribe(LoggedOnCallback::class.java, ::onLoggedOn)
        manager.subscribe(LoggedOffCallback::class.java, ::onLoggedOff)

        isRunning = true

        println("Connecting to steam...")

        // initiate the connection
        steamClient.connect()

        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onConnected(callback: ConnectedCallback) {
        println("Connected to Steam! Logging in $user...")

        val authSessionDetails = AuthSessionDetails().apply {
            username = user
            password = pass
            persistentSession = false
            authenticator = UserConsoleAuthenticator()
        }

        // get the authentication handler, which used for authenticating with Steam
        auth = SteamAuthentication(steamClient, unifiedMessages)

        runBlocking {
            supervisorScope {
                val authSession: CredentialsAuthSession = auth.beginAuthSessionViaCredentials(authSessionDetails)

                val deferredPolling = async {
                    authSession.pollingWaitForResult(this)
                }

                val pollResponse: AuthPollResult = deferredPolling.await()

                LogOnDetails().apply {
                    username = pollResponse.accountName
                    accessToken = pollResponse.refreshToken

                    // Set LoginID to a non-zero value if you have another client connected using the same account,
                    // the same private ip, and same public ip.
                    loginID = 149
                }.also(steamUser::logOn)

                // AccessToken can be used as the steamLoginSecure cookie
                // RefreshToken is required to generate new access tokens
                accessToken = pollResponse.accessToken
                refreshToken = pollResponse.refreshToken
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onDisconnected(callback: DisconnectedCallback) {
        println("Disconnected from Steam")

        isRunning = false
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        if (callback.result != EResult.OK) {
            println("Unable to login to steam: ${callback.result} / ${callback.extendedResult}")

            isRunning = false

            return
        }

        println("Successfully logged on!")

        // This is how you concatenate the cookie, you can set it on the Steam domains, and it should work
        // but actual usage of this will be left as an exercise for the reader
        @Suppress("UNUSED_VARIABLE")
        val steamLoginSecure = "${callback.clientSteamID.convertToUInt64()}||$accessToken"

        // The access token expires in 24 hours (at the time of writing) so you will have to renew it.
        // Parse this token with a JWT library to get the expiration date and set up a timer to renew it.
        // To renew you will have to call this:
        // When allowRenewal is set to true, Steam may return new RefreshToken
        val newTokens = auth.generateAccessTokenForApp(callback.clientSteamID, refreshToken, false)

        accessToken = newTokens.accessToken

        if (!Strings.isNullOrEmpty(newTokens.refreshToken)) {
            refreshToken = newTokens.refreshToken
        }

        // Do not forget to update steamLoginSecure with the new accessToken!

        // for this sample we'll just log off
        steamUser.logOff()
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        println("Logged off of Steam: " + callback.result)

        isRunning = false
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                println("SampleWebCookie: No username and password specified!")
                return
            }

            LogManager.addListener(DefaultLogListener())

            SampleWebCookieKt(args[0], args[1]).run()
        }
    }
}
