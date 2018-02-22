package in.dragonbra.javasteam.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the abstract base class for all available client messages.
 * It's used to maintain packet payloads and provide a header for all client messages.
 */
public abstract class MsgBase<HdrType extends ISteamSerializable> extends AbstractMsgBase implements IClientMsg {

    private static final Logger logger = LogManager.getLogger(MsgBase.class);

    private HdrType header;

    /**
     * Initializes a new instance of the {@link MsgBase} class.
     */
    public MsgBase(Class<HdrType> clazz) {
        this(clazz, 0);
    }

    /**
     * Initializes a new instance of the {@link MsgBase} class.
     *
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public MsgBase(Class<HdrType> clazz, int payloadReserve) {
        super(payloadReserve);
        try {
            header = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.debug(e);
        }
    }

    /**
     * Gets the header for this message type.
     */
    public HdrType getHeader() {
        return header;
    }
}
