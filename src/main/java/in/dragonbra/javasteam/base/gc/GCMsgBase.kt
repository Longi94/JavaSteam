package `in`.dragonbra.javasteam.base.gc

import `in`.dragonbra.javasteam.base.AbstractMsgBase
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.lang.reflect.InvocationTargetException

/**
 * This is the abstract base class for all available game coordinator messages.
 * It's used to maintain packet payloads and provide a header for all gc messages.
 * @constructor Initializes a new instance of the [GCMsgBase] class.
 * @param clazz The type of the header.
 * @param payloadReserve The number of bytes to initialize the payload capacity to.
 * @param HdrType The header type for this gc message.
 */
@Suppress("unused")
abstract class GCMsgBase<HdrType : IGCSerializableHeader> @JvmOverloads constructor(
    clazz: Class<HdrType>,
    payloadReserve: Int = 0,
) : AbstractMsgBase(payloadReserve),
    IClientGCMsg {

    companion object {
        private val logger: Logger = LogManager.getLogger(GCMsgBase::class.java)
    }

    /**
     * Gets the header for this message type.
     */
    lateinit var header: HdrType
        private set

    init {
        try {
            header = clazz.getDeclaredConstructor().newInstance()
        } catch (e: NoSuchMethodException) {
            logger.error(e)
        } catch (e: InstantiationException) {
            logger.error(e)
        } catch (e: IllegalAccessException) {
            logger.error(e)
        } catch (e: InvocationTargetException) {
            logger.error(e)
        }
    }
}
