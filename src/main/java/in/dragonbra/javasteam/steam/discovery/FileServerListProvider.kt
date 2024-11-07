package `in`.dragonbra.javasteam.steam.discovery

import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServer
import `in`.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServerList
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Server provider that stores servers in a file using protobuf.
 * @constructor Instantiates a [FileServerListProvider] object.
 * @param file the file that will store the servers
 */
class FileServerListProvider(private val file: File) : IServerListProvider {

    init {
        try {
            file.absoluteFile.parentFile?.mkdirs()
            file.createNewFile()
        } catch (e: IOException) {
            logger.error(e)
        }
    }

    override fun fetchServerList(): List<ServerRecord> = try {
        FileInputStream(file).use { fis ->
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
    } catch (e: FileNotFoundException) {
        logger.error("servers list file not found", e)
        emptyList()
    } catch (e: IOException) {
        logger.error("Failed to read server list file ${file.absolutePath}", e)
        emptyList()
    }

    override fun updateServerList(endpoints: List<ServerRecord>) {
        val builder = BasicServerList.newBuilder().apply {
            endpoints.forEach { endpoint ->
                addServers(
                    BasicServer.newBuilder().apply {
                        address = endpoint.host
                        port = endpoint.port
                        protocol = ProtocolTypes.code(endpoint.protocolTypes)
                    }
                )
            }
        }

        try {
            FileOutputStream(file, false).use { fos ->
                builder.build().writeTo(fos)
                fos.flush()
            }
        } catch (e: IOException) {
            logger.error("Failed to write servers to file ${file.absolutePath}", e)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(FileServerListProvider::class.java)
    }
}
