package `in`.dragonbra.javasteam.steam.cdn

import `in`.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.types.ChunkData
import `in`.dragonbra.javasteam.types.DepotManifest
import `in`.dragonbra.javasteam.util.SteamKitWebRequestException
import `in`.dragonbra.javasteam.util.Strings
import `in`.dragonbra.javasteam.util.compat.readNBytesCompat
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.MemoryStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.IOException
import java.util.concurrent.*
import java.util.zip.DataFormatException
import java.util.zip.ZipInputStream

/**
 * The [Client] class is used for downloading game content from the Steam servers.
 * @constructor Initializes a new instance of the [Client] class.
 * @param steamClient The [SteamClient] this instance will be associated with.
 * The SteamClient instance must be connected and logged onto Steam.
 */
class Client(steamClient: SteamClient) : Closeable {

    private val httpClient: OkHttpClient = steamClient.configuration.httpClient

    private val defaultScope = CoroutineScope(Dispatchers.IO)

    companion object {

        private val logger: Logger = LogManager.getLogger(Client::class.java)

        /**
         * Default timeout to use when making requests
         */
        var requestTimeout = 10000L

        /**
         * Default timeout to use when reading the response body
         */
        var responseBodyTimeout = 60000L

        @JvmStatic
        @JvmOverloads
        fun buildCommand(
            server: Server,
            command: String,
            query: String? = null,
            proxyServer: Server? = null,
        ): HttpUrl {
            // TODO look into this to mimic SK's method. Should be able to remove if/else and only have the if.
            val httpUrl: HttpUrl
            if (proxyServer != null && proxyServer.useAsProxy && proxyServer.proxyRequestPathTemplate != null) {
                httpUrl = HttpUrl.Builder()
                    .scheme(if (proxyServer.protocol == Server.ConnectionProtocol.HTTP) "http" else "https")
                    .host(proxyServer.vHost)
                    .port(proxyServer.port)
                    .addPathSegment(server.vHost)
                    .addPathSegments(command)
                    .run {
                        query?.let { this.query(it) } ?: this
                    }.build()
            } else {
                httpUrl = HttpUrl.Builder()
                    .scheme(if (server.protocol == Server.ConnectionProtocol.HTTP) "http" else "https")
                    .host(server.vHost)
                    .port(server.port)
                    .addPathSegments(command)
                    .run {
                        query?.let { this.query(it) } ?: this
                    }.build()
            }

            return httpUrl
        }
    }

    /**
     * Disposes of this object.
     */
    override fun close() {
        httpClient.connectionPool.evictAll()
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
     * @exception DataFormatException When the data received is not as expected
     */
    suspend fun downloadManifest(
        depotId: Int,
        manifestId: Long,
        manifestRequestCode: ULong,
        server: Server,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): DepotManifest {
        val manifestVersion = 5
        val url = if (manifestRequestCode > 0U) {
            "depot/$depotId/manifest/$manifestId/$manifestVersion/$manifestRequestCode"
        } else {
            "depot/$depotId/manifest/$manifestId/$manifestVersion"
        }

        val request = Request.Builder()
            .url(buildCommand(server, url, cdnAuthToken, proxyServer))
            .build()

        return withTimeout(requestTimeout) {
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw SteamKitWebRequestException(
                    "Response status code does not indicate success: ${response.code} (${response.message})",
                    response
                )
            }

            val depotManifest = withTimeout(responseBodyTimeout) {
                val contentLength = response.header("Content-Length")?.toIntOrNull()

                if (contentLength == null) {
                    logger.debug("Manifest response does not have Content-Length, falling back to unbuffered read.")
                }

                response.body.byteStream().use { inputStream ->
                    ByteArrayOutputStream().use { bs ->
                        val bytesRead = inputStream.copyTo(bs, contentLength ?: DEFAULT_BUFFER_SIZE)

                        if (bytesRead != contentLength?.toLong()) {
                            throw DataFormatException("Length mismatch after downloading depot manifest! (was $bytesRead, but should be $contentLength)")
                        }

                        val contentBytes = bs.toByteArray()

                        MemoryStream(contentBytes).use { ms ->
                            ZipInputStream(ms).use { zip ->
                                var entryCount = 0
                                while (zip.nextEntry != null) {
                                    entryCount++
                                }
                                if (entryCount > 1) {
                                    logger.debug("Expected the zip to contain only one file")
                                }
                            }
                        }

                        // Decompress the zipped manifest data
                        MemoryStream(contentBytes).use { ms ->
                            ZipInputStream(ms).use { zip ->
                                zip.nextEntry
                                DepotManifest.deserialize(zip)
                            }
                        }
                    }
                }
            }

            depotKey?.let { key ->
                // if we have the depot key, decrypt the manifest filenames
                depotManifest.decryptFilenames(key)
            }

            depotManifest
        }
    }

    /**
     * Java Compat:
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
     * @exception DataFormatException When the data received is not as expected
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
    ): CompletableFuture<DepotManifest> = defaultScope.future {
        return@future downloadManifest(
            depotId = depotId,
            manifestId = manifestId,
            manifestRequestCode = manifestRequestCode.toULong(),
            server = server,
            depotKey = depotKey,
            proxyServer = proxyServer,
            cdnAuthToken = cdnAuthToken,
        )
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
    ): Int {
        require(chunk.chunkID != null) { "Chunk must have a ChunkID." }

        if (depotKey == null) {
            if (destination.size < chunk.compressedLength) {
                throw IllegalArgumentException("The destination buffer must be longer than the chunk CompressedLength (since no depot key was provided).")
            }
        } else {
            if (destination.size < chunk.uncompressedLength) {
                throw IllegalArgumentException("The destination buffer must be longer than the chunk UncompressedLength.")
            }
        }

        val chunkID = Strings.toHex(chunk.chunkID)
        val url = "depot/$depotId/chunk/$chunkID"

        val request: Request = if (ClientLancache.useLanCacheServer) {
            ClientLancache.buildLancacheRequest(server, url, cdnAuthToken)
        } else {
            Request.Builder().url(buildCommand(server, url, cdnAuthToken, proxyServer)).build()
        }

        withTimeout(requestTimeout) {
            httpClient.newCall(request).execute()
        }.use { response ->
            if (!response.isSuccessful) {
                throw SteamKitWebRequestException(
                    "Response status code does not indicate success: ${response.code} (${response.message})",
                    response
                )
            }

            var contentLength = chunk.compressedLength

            response.header("Content-Length")?.toLongOrNull()?.let { responseContentLength ->
                contentLength = responseContentLength.toInt()

                // assert that lengths match only if the chunk has a length assigned.
                if (chunk.compressedLength > 0 && contentLength != chunk.compressedLength) {
                    throw IllegalStateException("Content-Length mismatch for depot chunk! (was $contentLength, but should be ${chunk.compressedLength})")
                }
            } ?: run {
                if (contentLength > 0) {
                    logger.debug("Response does not have Content-Length, falling back to chunk.compressedLength.")
                } else {
                    throw SteamKitWebRequestException(
                        "Response does not have Content-Length and chunk.compressedLength is not set.",
                        response
                    )
                }
            }

            // If no depot key is provided, stream into the destination buffer without renting
            if (depotKey == null) {
                val bytesRead = withTimeout(responseBodyTimeout) {
                    response.body.byteStream().use { input ->
                        input.readNBytesCompat(destination, 0, contentLength)
                    }
                }

                if (bytesRead != contentLength) {
                    throw IOException("Length mismatch after downloading depot chunk! (was $bytesRead, but should be $contentLength)")
                }

                return contentLength
            }

            // We have to stream into a temporary buffer because a decryption will need to be performed
            val buffer = ByteArray(contentLength)

            try {
                val bytesRead = withTimeout(responseBodyTimeout) {
                    response.body.byteStream().use { input ->
                        input.readNBytesCompat(buffer, 0, contentLength)
                    }
                }

                if (bytesRead != contentLength) {
                    throw IOException("Length mismatch after downloading encrypted depot chunk! (was $bytesRead, but should be $contentLength)")
                }

                // process the chunk immediately
                return DepotChunk.process(chunk, buffer, destination, depotKey)
            } catch (ex: Exception) {
                logger.error("Failed to download a depot chunk ${request.url}", ex)
                throw ex
            }
        }
    }

    /**
     * Java Compat:
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
    @JvmOverloads
    fun downloadDepotChunkFuture(
        depotId: Int,
        chunk: ChunkData,
        server: Server,
        destination: ByteArray,
        depotKey: ByteArray? = null,
        proxyServer: Server? = null,
        cdnAuthToken: String? = null,
    ): CompletableFuture<Int> = defaultScope.future {
        return@future downloadDepotChunk(
            depotId = depotId,
            chunk = chunk,
            server = server,
            destination = destination,
            depotKey = depotKey,
            proxyServer = proxyServer,
            cdnAuthToken = cdnAuthToken,
        )
    }
}
