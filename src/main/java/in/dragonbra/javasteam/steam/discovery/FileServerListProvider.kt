package `in`.dragonbra.javasteam.steam.discovery

import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServer
import `in`.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServerList
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.time.Instant

/**
 * Server provider that stores servers in a file using protobuf.
 *
 * @constructor Initialize a new instance of FileStorageServerListProvider
 * @param file the filename that will store the servers
 */
class FileServerListProvider(val file: Path) : IServerListProvider {

    /**
     * Instantiates a [FileServerListProvider] object.
     * @param file the file that will store the servers
     */
    constructor(file: File) : this(file.toPath())

    /**
     * Instantiates a [FileServerListProvider] object.
     * @param filename the filename that will store the servers.
     */
    constructor(filename: String) : this(Path.of(filename))

    init {
        require(file.fileName.toString().isNotBlank()) { "FileName must not be blank" }
    }

    /**
     * Returns the last time the file was written on disk
     */
    override val lastServerListRefresh: Instant
        get() = Files.getLastModifiedTime(file).toInstant()

    /**
     * Read the stored list of servers from the file
     * @return List of servers if persisted, otherwise an empty list
     */
    override fun fetchServerList(): List<ServerRecord> = runCatching {
        Files.newInputStream(file).use { fis ->
            val serverList = BasicServerList.parseFrom(fis)
            List(serverList.serversCount) { i ->
                val server: BasicServer = serverList.getServers(i)
                ServerRecord.createServer(
                    server.getAddress(),
                    server.port,
                    ProtocolTypes.from(server.protocol)
                )
            }
        }
    }.fold(
        onSuccess = { it },
        onFailure = { error ->
            when (error) {
                is NoSuchFileException -> logger.debug("File doesn't exist")
                else -> logger.error("Unknown error occurred", error)
            }

            emptyList()
        }
    )

    /**
     * Writes the supplied list of servers to persistent storage
     * @param endpoints List of server endpoints
     */
    override fun updateServerList(endpoints: List<ServerRecord>) {
        val builder = BasicServerList.newBuilder().apply {
            addAllServers(
                endpoints.map { endpoint ->
                    BasicServer.newBuilder()
                        .setAddress(endpoint.host)
                        .setPort(endpoint.port)
                        .setProtocol(ProtocolTypes.code(endpoint.protocolTypes))
                        .build()
                }
            )
        }

        try {
            Files.newOutputStream(file).use { fos ->
                builder.build().writeTo(fos)
            }
        } catch (e: IOException) {
            logger.error("Failed to write servers to file ${file.fileName}", e)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(FileServerListProvider::class.java)
    }
}
