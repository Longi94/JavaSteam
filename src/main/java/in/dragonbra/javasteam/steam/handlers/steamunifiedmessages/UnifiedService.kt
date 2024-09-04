package `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages

import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.util.log.LogManager
import java.lang.reflect.Proxy

/**
 * @author Lossy
 * @since 2023-01-04
 *
 * This wrapper is used for expression-based RPC calls using Steam Unified Messaging.
 *
 * var playService = steamUnifiedMessages.createService(IPlayer.class);
 * favoriteBadge = playService.sendMessage(api ->
 *         api.GetFavoriteBadge(favoriteBadgeRequest.build())
 * ).runBlocking();
 */
@Suppress("unused")
class UnifiedService<TService : Any>(
    private val serviceClass: Class<TService>,
    private val steamUnifiedMessages: SteamUnifiedMessages,
) {
    private companion object {
        private val logger = LogManager.getLogger(UnifiedService::class.java)
    }

    /**
     * Sends a message.
     * Results are returned in a [ServiceMethodResponse].
     * The returned [AsyncJobSingle] can also be awaited to retrieve the callback result.
     * @param TRequest The type of the protobuf object which is the request to the RPC call
     * @param TResponse The type of the protobuf object which is the response to the RPC call.
     * @param expr RPC call expression, e.g. x => x.SomeMethodCall(message);
     * @return The JobID of the request. This can be used to find the appropriate [ServiceMethodResponse].
     */
    fun <TRequest : GeneratedMessage.Builder<TRequest>, TResponse : GeneratedMessage> sendMessage(
        expr: (TService) -> TResponse,
    ): AsyncJobSingle<ServiceMethodResponse> = sendMessageOrNotification(expr, false)!!

    /**
     * Sends a notification.
     * @param TRequest The type of the protobuf object which is the request to the RPC call.
     * @param TResponse The type of the protobuf object which is the response to the RPC call.
     * @param expr RPC call expression, e.g. x => x.SomeMethodCall(message);
     * @return null
     */
    fun <TRequest : GeneratedMessage.Builder<TRequest>, TResponse : GeneratedMessage> sendNotification(
        expr: (TService) -> TResponse,
    ): AsyncJobSingle<ServiceMethodResponse>? = sendMessageOrNotification(expr, true)

    private fun <TRequest : GeneratedMessage.Builder<TRequest>, TResponse : GeneratedMessage> sendMessageOrNotification(
        expr: (TService) -> TResponse,
        isNotification: Boolean,
    ): AsyncJobSingle<ServiceMethodResponse>? {
        var methodName: String? = null
        var methodArgs: GeneratedMessage? = null

        @Suppress("UNCHECKED_CAST")
        val proxy = Proxy.newProxyInstance(
            serviceClass.classLoader,
            arrayOf(serviceClass)
        ) { _, method, args ->
            methodName = method.name
            methodArgs = args.firstOrNull() as GeneratedMessage
            null
        } as TService
        expr(proxy)

        val version = 1
        val serviceName = serviceClass.simpleName.removePrefix("I")
        val rpcName = "$serviceName.$methodName#$version"

        requireNotNull(methodArgs) { "Unable to get arguments for message" }

        logger.debug("Service Name: $rpcName / Notification: $isNotification")
        return if (isNotification) {
            steamUnifiedMessages.sendNotification<TRequest>(rpcName, methodArgs!!)
            null
        } else {
            steamUnifiedMessages.sendMessage<TRequest>(rpcName, methodArgs!!)
        }
    }
}
