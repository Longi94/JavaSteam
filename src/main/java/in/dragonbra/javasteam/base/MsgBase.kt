package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * This is the abstract base class for all available client messages.
 * It's used to maintain packet payloads and provide a header for all client messages.
 */
@SuppressWarnings("unused")
public abstract class MsgBase<HdrType extends ISteamSerializable> extends AbstractMsgBase implements IClientMsg {

    private static final Logger logger = LogManager.getLogger(MsgBase.class);

    private HdrType header;

    /**
     * Initializes a new instance of the {@link MsgBase} class.
     *
     * @param clazz the type of the header
     */
    public MsgBase(Class<HdrType> clazz) {
        this(clazz, 0);
    }

    /**
     * Initializes a new instance of the {@link MsgBase} class.
     *
     * @param clazz          the type of the header
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public MsgBase(Class<HdrType> clazz, int payloadReserve) {
        super(payloadReserve);
        try {
            header = clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException |
                 InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException e) {
            logger.debug(e);
        }
    }

    /**
     * @return the header for this message type.
     */
    public HdrType getHeader() {
        return header;
    }
}
