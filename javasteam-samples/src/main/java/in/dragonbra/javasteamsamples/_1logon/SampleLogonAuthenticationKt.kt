package `in`.dragonbra.javasteamsamples._1logon

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
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
import `in`.dragonbra.javasteam.util.log.DefaultLogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import java.util.concurrent.CancellationException

/**
 * @author lossy
 * @since 2023-03-19
 */
class SampleLogonAuthenticationKt(private val user: String, private val pass: String) : Runnable {

    private lateinit var manager: CallbackManager

    private lateinit var steamClient: SteamClient

    private lateinit var steamUser: SteamUser

    private lateinit var unifiedMessages: SteamUnifiedMessages

    private var isRunning = false

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

        val authDetails = AuthSessionDetails().apply {
            username = user
            password = pass
            persistentSession = false
            authenticator = UserConsoleAuthenticator()
        }

        try {
            // get the authentication handler, which used for authenticating with Steam
            val auth = SteamAuthentication(steamClient, unifiedMessages)
            val authSession: CredentialsAuthSession = auth.beginAuthSessionViaCredentials(authDetails)

            runBlocking {
                supervisorScope {
                    val deferredPolling = async {
                        authSession.pollingWaitForResult(this)
                    }

                    /**
                     * This demonstrates that we can cancel polling
                     */
                    delay(20000L)
                    deferredPolling.cancel(CancellationException("Forced Cancel from sample!"))
                    deferredPolling.join()

                    val pollResponse: AuthPollResult = deferredPolling.await()

                    val details = LogOnDetails().apply {
                        username = pollResponse.accountName
                        accessToken = pollResponse.refreshToken
                    }

                    // Set LoginID to a non-zero value if you have another client connected using the same account,
                    // the same private ip, and same public ip.
                    details.loginID = 149

                    steamUser.logOn(details)

                    // This is not required, but it is possible to parse the JWT access token to see the scope and expiration date.
                    // parseJsonWebToken(pollResponse.accessToken, "AccessToken");
                    // parseJsonWebToken(pollResponse.refreshToken, "RefreshToken");
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is AuthenticationException -> println("An Authentication error has occurred.")
                is CancellationException -> println("An Cancellation exception was raised. Usually means a timeout occurred")
            }

            steamClient.disconnect()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onDisconnected(callback: DisconnectedCallback) {
        println("Disconnected from Steam")
        isRunning = false
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        if (callback.result != EResult.OK) {
            println("Unable to logon to Steam: " + callback.result + " / " + callback.extendedResult)

            isRunning = false

            return
        }

        println("Successfully logged on!")

        // at this point, we'd be able to perform actions on Steam

        // for this sample we'll just log off
        steamUser.logOff()
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        println("Logged off of Steam: " + callback.result)

        isRunning = false
    }

    @Suppress("unused")
    private fun parseJsonWebToken(token: String, name: String) {
        val tokenComponents = token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Fix up base64url to normal base64
        var base64 = tokenComponents[1].replace('-', '+').replace('_', '/')
        if (base64.length % 4 != 0) {
            base64 += String(CharArray(4 - base64.length % 4)).replace('\u0000', '=')
        }
        val payloadBytes = Base64.getDecoder().decode(base64)

        // Payload can be parsed as JSON, and then fields such expiration date, scope, etc can be accessed
        val gson = GsonBuilder().setPrettyPrinting().create()
        val payload = JsonParser.parseString(String(payloadBytes))
        val formatted = gson.toJson(payload)

        // For brevity, we will simply output formatted json to console
        println("$name: $formatted\n")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                println("Sample1: No username and password specified!")
                return
            }

            LogManager.addListener(DefaultLogListener())
            SampleLogonAuthenticationKt(args[0], args[1]).run()
        }
    }
}