package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.util.SteamKitWebRequestException
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.zip.ZipInputStream

/**
 * The [Client] class is used for downloading game content from the Steam servers.
 * @constructor Initializes a new instance of the [Client] class.
 * @param steamClient The [SteamClient] this instance will be associated with.
 * The SteamClient instance must be connected and logged onto Steam.
 */
class Client(steamClient: SteamClient) : Closeable {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {

        private val logger: Logger = LogManager.getLogger(Client::class.java)

        private fun buildCommand(
            server: Server,
            command: String,
            query: String? = null,
            proxyServer: Server? = null,
        ): HttpUrl {
            val scheme = if (server.protocol == Server.ConnectionProtocol.HTTP) "http" else "https"
            var host = server.vHost ?: server.host ?: ""
            var port = server.port
            var path = command

            if (proxyServer != null && proxyServer.useAsProxy && proxyServer.proxyRequestPathTemplate != null) {
                val pathTemplate = proxyServer.proxyRequestPathTemplate!!
                    .replace("%host%", host)
                    .replace("%path%", "/$command")

                host = proxyServer.vHost ?: proxyServer.host ?: ""
                port = proxyServer.port
                path = pathTemplate
            }

            val urlBuilder = HttpUrl.Builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .addPathSegments(path.trimStart('/'))

            query?.let { queryString ->
                if (queryString.isNotEmpty()) {
                    val params = queryString.split("&")
                    for (param in params) {
                        val keyValue = param.split("=", limit = 2)
                        if (keyValue.size == 2) {
                            urlBuilder.addQueryParameter(keyValue[0], keyValue[1])
                        } else if (keyValue.size == 1 && keyValue[0].isNotEmpty()) {
                            urlBuilder.addQueryParameter(keyValue[0], "")
                        }
                    }
                }
            }

            return urlBuilder.build()
        }
    }

    private val httpClient: OkHttpClient = steamClient.configuration.httpClient

    /**
     * Disposes of this object.
     */
    override fun close() {
        scope.cancel()
        httpClient.connectionPool.evictAll()
        httpClient.dispatcher.executorService.shutdown()
    }

    /**
     * Downloads the depot manifest specified by the given manifest ID, and optionally decrypts the manifest's filenames if the depot decryption key has been provided.
     * @param depotId The id of the depot being accessed.
     * @param manifestId The unique identifier of the manifest to be downloaded.
     * @param manifestRequestCode The manifest request code for the manifest that is being downloaded.
     * @param server The content server to connect to.
     * @param depotKey The depot decryption key for the depot that will be downloaded.
     * This is used for decrypting filenames (if needed) in depot manifests.
     * @param proxyServer Optional content server marked as UseAsProxy which transforms the request.
     * @param cdnAuthToken CDN auth token for CDN content server endpoints if necessary. Get one with [SteamContent.getCDNAuthToken].
     * @return A [DepotManifest] instance that contains information about the files present within a depot.
     * @exception IllegalArgumentException [server] was null.
     * @exception IOException A network error occurred when performing the request.
     * @exception SteamKitWebRequestException A network error occurred when performing the request.
     */
    suspend fun downloadManifest(
        depotId: Int,
        manifestId: Long,
        manifestRequestCode: ULong,
        server: Server,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): DepotManifest = withContext(Dispatchers.IO) {
        val manifestVersion = 5

        val url = if (manifestRequestCode > 0U) {
            "depot/$depotId/manifest/$manifestId/$manifestVersion/$manifestRequestCode"
        } else {
            "depot/$depotId/manifest/$manifestId/$manifestVersion"
        }

        val request = Request.Builder()
            .url(buildCommand(server, url, cdnAuthToken, proxyServer))
            .build()

        logger.debug("Request URL is: $request")

        try {
            val response = httpClient.newCall(request).executeAsync()

            if (!response.isSuccessful) {
                throw SteamKitWebRequestException(
                    "Response status code does not indicate success: ${response.code} (${response.message})",
                    response
                )
            }

            return@withContext response.use { resp ->
                val responseBody = resp.body?.bytes()
                    ?: throw SteamKitWebRequestException("Response body is null")

                if (responseBody.isEmpty()) {
                    throw SteamKitWebRequestException("Response is empty")
                }

                // Decompress the zipped manifest data
                ZipInputStream(ByteArrayInputStream(responseBody)).use { zipInputStream ->
                    zipInputStream.nextEntry
                        ?: throw SteamKitWebRequestException("Expected the zip to contain at least one file")

                    val manifestData = zipInputStream.readBytes()

                    val depotManifest = DepotManifest.deserialize(ByteArrayInputStream(manifestData))

                    if (depotKey != null) {
                        depotManifest.decryptFilenames(depotKey)
                    }

                    depotManifest
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to download manifest ${request.url}: ${e.message}", e)
            throw e
        }
    }

    /**
     * Downloads the specified depot chunk, and optionally processes the chunk and verifies the checksum if the depot decryption key has been provided.
     * This function will also validate the length of the downloaded chunk with the value of [ChunkData.compressedLength],
     * if it has been assigned a value.
     * @param depotId The id of the depot being accessed.
     * @param chunk A [ChunkData] instance that represents the chunk to download.
     * This value should come from a manifest downloaded with [downloadManifest].
     * @param server The content server to connect to.
     * @param destination The buffer to receive the chunk data. If [depotKey] is provided, this will be the decompressed buffer.
     * Allocate or rent a buffer that is equal or longer than [ChunkData.uncompressedLength]
     * @param depotKey The depot decryption key for the depot that will be downloaded.
     * This is used to process the chunk data.
     * @param proxyServer Optional content server marked as UseAsProxy which transforms the request.
     * @param cdnAuthToken CDN auth token for CDN content server endpoints if necessary. Get one with [SteamContent.getCDNAuthToken].
     * @return The total number of bytes written to [destination].
     * @exception IllegalArgumentException Thrown if the chunk's [ChunkData.chunkID] was null or if the [destination] buffer is too small.
     * @exception IllegalStateException Thrown if the downloaded data does not match the expected length.
     * @exception SteamKitWebRequestException A network error occurred when performing the request.
     */
    suspend fun downloadDepotChunk(
        depotId: Int,
        chunk: ChunkData,
        server: Server,
        destination: ByteArray,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): Int = withContext(Dispatchers.IO) {
        require(chunk.chunkID != null) { "Chunk must have a ChunkID." }

        if (depotKey == null) {
            if (destination.size < chunk.compressedLength) {
                throw IllegalArgumentException("The destination buffer must be longer than the chunk CompressedLength (since no depot key was provided).")
            }
        } else {
            if (destination.size != chunk.compressedLength) {
                throw IllegalArgumentException("The destination buffer must be the same size as the chunk UncompressedLength.")
            }
        }

        val chunkID = Strings.toHex(chunk.chunkID)
        val url = "depot/$depotId/chunk/$chunkID"

        val request = if (ClientLancache.useLanCacheServer) {
            ClientLancache.buildLancacheRequest(server = server, command = url, query = cdnAuthToken)
        } else {
            val url = buildCommand(server = server, command = url, query = cdnAuthToken, proxyServer = proxyServer)
            Request.Builder()
                .url(url)
                .build()
        }

        try {
            val response = httpClient.newCall(request).executeAsync()

            response.use { resp ->
                if (!resp.isSuccessful) {
                    throw SteamKitWebRequestException(
                        "Response status code does not indicate success: ${resp.code} (${resp.message})",
                        resp
                    )
                }

                var contentLength = chunk.compressedLength

                if (resp.body.contentLength().toInt() > 0) {
                    contentLength = resp.body.contentLength().toInt()

                    // assert that lengths match only if the chunk has a length assigned.
                    if (chunk.compressedLength > 0 && contentLength != chunk.compressedLength) {
                        throw SteamKitWebRequestException(
                            "Content-Length mismatch for depot chunk! (was $contentLength, but should be ${chunk.compressedLength})"
                        )
                    }
                } else if (contentLength > 0) {
                    logger.debug("Response does not have Content-Length, falling back to chunk.CompressedLength.")
                } else {
                    throw SteamKitWebRequestException(
                        "Response does not have Content-Length and chunk.CompressedLength is not set.",
                        response
                    )
                }

                val responseBody = resp.body.bytes()

                if (responseBody.isEmpty()) {
                    throw SteamKitWebRequestException("Response is empty")
                }

                if (responseBody.size != contentLength) {
                    throw SteamKitWebRequestException(
                        "Length mismatch after downloading depot chunk! (was ${responseBody.size}, but should be $contentLength)"
                    )
                }

                System.arraycopy(responseBody, 0, destination, 0, contentLength)
                return@withContext contentLength
            }
        } catch (e: Exception) {
            logger.error("Failed to download a depot chunk ${request.url}: ${e.message}", e)
            throw e
        }
    }

    // region Java Compatibility

    /**
     * Java-compatible version of downloadManifest that returns a CompletableFuture.
     * Downloads the depot manifest specified by the given manifest ID, and optionally decrypts the manifest's filenames if the depot decryption key has been provided.
     * @param depotId The id of the depot being accessed.
     * @param manifestId The unique identifier of the manifest to be downloaded.
     * @param manifestRequestCode The manifest request code for the manifest that is being downloaded.
     * @param server The content server to connect to.
     * @param depotKey The depot decryption key for the depot that will be downloaded.
     * This is used for decrypting filenames (if needed) in depot manifests.
     * @param proxyServer Optional content server marked as UseAsProxy which transforms the request.
     * @param cdnAuthToken CDN auth token for CDN content server endpoints if necessary. Get one with [SteamContent.getCDNAuthToken].
     * @return A CompletableFuture that will complete with a [DepotManifest] instance that contains information about the files present within a depot.
     * @exception IllegalArgumentException [server] was null.
     * @exception IOException A network error occurred when performing the request.
     * @exception SteamKitWebRequestException A network error occurred when performing the request.
     */
    @JvmOverloads
    fun downloadManifestFuture(
        depotId: Int,
        manifestId: Long,
        manifestRequestCode: Long,
        server: Server,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): CompletableFuture<DepotManifest> {
        val future = CompletableFuture<DepotManifest>()

        scope.launch {
            try {
                val result = downloadManifest(
                    depotId = depotId,
                    manifestId = manifestId,
                    manifestRequestCode = manifestRequestCode.toULong(),
                    server = server,
                    depotKey = depotKey,
                    proxyServer = proxyServer,
                    cdnAuthToken = cdnAuthToken
                )
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    /**
     * Java-compatible version of downloadDepotChunk that returns a CompletableFuture.
     * Downloads the specified depot chunk, and optionally processes the chunk and verifies the checksum if the depot decryption key has been provided.
     * This function will also validate the length of the downloaded chunk with the value of [ChunkData.compressedLength],
     * if it has been assigned a value.
     * @param depotId The id of the depot being accessed.
     * @param chunk A [ChunkData] instance that represents the chunk to download.
     * This value should come from a manifest downloaded with [downloadManifest].
     * @param server The content server to connect to.
     * @param destination The buffer to receive the chunk data. If [depotKey] is provided, this will be the decompressed buffer.
     * Allocate or rent a buffer that is equal or longer than [ChunkData.uncompressedLength]
     * @param depotKey The depot decryption key for the depot that will be downloaded.
     * This is used to process the chunk data.
     * @param proxyServer Optional content server marked as UseAsProxy which transforms the request.
     * @param cdnAuthToken CDN auth token for CDN content server endpoints if necessary. Get one with [SteamContent.getCDNAuthToken].
     * @return A CompletableFuture that will complete with the total number of bytes written to [destination].
     * @exception IllegalArgumentException Thrown if the chunk's [ChunkData.chunkID] was null or if the [destination] buffer is too small.
     * @exception IllegalStateException Thrown if the downloaded data does not match the expected length.
     * @exception SteamKitWebRequestException A network error occurred when performing the request.
     */
    @JvmOverloads
    fun downloadDepotChunkFuture(
        depotId: Int,
        chunk: ChunkData,
        server: Server,
        destination: ByteArray,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()

        scope.launch {
            try {
                val downloadedBytes = ByteArray(chunk.compressedLength)

                val bytesDownloaded = downloadDepotChunk(
                    depotId = depotId,
                    chunk = chunk,
                    server = server,
                    destination = downloadedBytes,
                    depotKey = depotKey,
                    proxyServer = proxyServer,
                    cdnAuthToken = cdnAuthToken
                )

                if (depotKey != null) {
                    val bytesProcessed = DepotChunk.process(chunk, downloadedBytes, destination, depotKey)
                    future.complete(bytesProcessed)
                } else {
                    System.arraycopy(downloadedBytes, 0, destination, 0, bytesDownloaded)
                    future.complete(bytesDownloaded)
                }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    // endregion
}
