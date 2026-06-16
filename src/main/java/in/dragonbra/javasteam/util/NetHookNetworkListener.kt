package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.util.log.LogManager
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

/**
 * Dump any network messages sent to and received from the Steam server that the client is connected to.
 * These messages are dumped to file, and can be analyzed further with NetHookAnalyzer, a hex editor, or your own purpose-built tools.
 * Be careful with this, sensitive data may be written to the disk (such as your Steam password).
 */
class NetHookNetworkListener @JvmOverloads constructor(path: String = "netlogs") : IDebugNetworkListener {

    companion object {
        private val logger = LogManager.getLogger<NetHookNetworkListener>()

        private val FORMAT = SimpleDateFormat("yyyy_MM_dd_H_m_s_S")
    }

    private val messageNumber = AtomicLong(0L)

    private val logDirectory: File

    init {
        val dir = File(path)
        dir.mkdir()

        logDirectory = File(dir, FORMAT.format(Date()))
        logDirectory.mkdir()
    }

    override fun onIncomingNetworkMessage(msgType: EMsg, data: ByteArray) {
        logger.debug("<- Recv'd EMsg: $msgType (${msgType.code()})")

        try {
            val file = File(logDirectory, getFile("in", msgType))
            val path = Paths.get(file.absolutePath)
            Files.write(path, data)
        } catch (e: IOException) {
            logger.debug(e)
        }
    }

    override fun onOutgoingNetworkMessage(msgType: EMsg, data: ByteArray) {
        logger.debug("Sent -> EMsg: $msgType")

        try {
            val file = File(logDirectory, getFile("out", msgType))
            val path = Paths.get(file.absolutePath)
            Files.write(path, data)
        } catch (e: IOException) {
            logger.debug(e)
        }
    }

    private fun getFile(direction: String, msgType: EMsg): String =
        "${messageNumber.getAndIncrement()}_${direction}_${msgType.code()}_k_EMsg$msgType.bin"
}
