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
import `in`.dragonbra.javasteam.util.log.DefaultLogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.coroutines.*
import pro.leaco.console.qrcode.ConsoleQrcode.print
import java.lang.Runnable
import java.util.concurrent.CancellationException

// TODO: https://github.com/SteamRE/SteamKit/pull/1129#issuecomment-1473758793
//  CM will kick you in 60 seconds if you don't auth.
//  Steam client also has this issue, but it will reconnect and just continue polling.

/**
 * Same example as SampleLogonQRAuthentication.java but in Kotlin
 *
 * @author lossy
 * @since 2023-03-19
 */
class SampleLogonQRAuthenticationKt : Runnable, OnChallengeUrlChanged {

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
        try {
            // get the authentication handler, which used for authenticating with Steam
            val auth = SteamAuthentication(steamClient, unifiedMessages)
            val authSession: QrAuthSession = auth.beginAuthSessionViaQR(AuthSessionDetails())

            // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
            // Note: Callback is below.
            authSession.challengeUrlChanged = this

            // Draw current qr right away
            drawQRCode(authSession)

            // Starting polling Steam for authentication response
            // This response is later used to log on to Steam after connecting
            runBlocking(Dispatchers.IO) {
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

                    val pollResponse = deferredPolling.await()

                    println("Connected to Steam! Logging in " + pollResponse.accountName + "...")

                    val details = LogOnDetails().apply {
                        username = pollResponse.accountName
                        accessToken = pollResponse.refreshToken
                    }

                    details.loginID = 149

                    steamUser.logOn(details)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            when (e) {
                is CancellationException -> println("An Cancellation exception was raised. Usually means a timeout occurred")
                is AuthenticationException -> println("An Authentication error has occurred.")
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

    private fun drawQRCode(authSession: QrAuthSession) {
        val challengeURL: String = authSession.challengeUrl

        println("Challenge URL: $challengeURL\n")

        // Encode and Print the link as a QR code
        println("Use the Steam Mobile App to sign in via QR code:")

        print(challengeURL)
    }

    override fun onChanged(qrAuthSession: QrAuthSession) {
        drawQRCode(qrAuthSession)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // No username or password is required for this example.
            LogManager.addListener(DefaultLogListener())
            SampleLogonQRAuthenticationKt().run()
        }
    }
}
