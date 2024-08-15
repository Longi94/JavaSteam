package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EMarketingMessageFlags
import `in`.dragonbra.javasteam.generated.MsgClientMarketingMessageUpdate2
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.GlobalID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import `in`.dragonbra.javasteam.util.stream.BinaryReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * This callback is fired when the client receives a marketing message update.
 */
class MarketingMessageCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets the time of this marketing message update.
     */
    val updateTime: Date

    /**
     * Gets the messages.
     */
    val messages: List<Message>

    init {
        val marketingMessage = ClientMsg(MsgClientMarketingMessageUpdate2::class.java, packetMsg)
        val body = marketingMessage.body

        updateTime = Date(body.marketingMessageUpdateTime * 1000L)

        val msgList: MutableList<Message> = ArrayList()

        try {
            BinaryReader(ByteArrayInputStream(marketingMessage.payload.toByteArray())).use { br ->
                for (i in 0 until body.count) {
                    val dataLen = br.readInt() - 4 // total length includes the 4 byte length
                    val messageData = br.readBytes(dataLen)

                    msgList.add(Message(messageData))
                }
            }
        } catch (e: IOException) {
            logger.debug(e)
        }

        messages = msgList.toList()
    }

    /**
     * Represents a single marketing message.
     */
    class Message internal constructor(data: ByteArray?) {

        /**
         * Gets the unique identifier for this marketing message.
         */
        var id: GlobalID? = null
            private set

        /**
         * Gets the URL for this marketing message.
         */
        var url: String? = null
            private set

        /**
         * Gets the marketing message flags.
         */
        var flags: EnumSet<EMarketingMessageFlags>? = null
            private set

        init {
            try {
                BinaryReader(ByteArrayInputStream(data)).use { br ->
                    id = GlobalID(br.readLong())
                    url = br.readNullTermString(StandardCharsets.UTF_8)
                    flags = EMarketingMessageFlags.from(br.readInt())
                }
            } catch (e: IOException) {
                logger.debug(e)
            }
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MarketingMessageCallback::class.java)
    }
}
