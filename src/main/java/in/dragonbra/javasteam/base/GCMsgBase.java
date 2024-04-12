package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.lang.reflect.InvocationTargetException;

/**
 * This is the abstract base class for all available game coordinator messages.
 * It's used to maintain packet payloads and provide a header for all gc messages.
 *
 * @param <HdrType> The header type for this gc message.
 */
@SuppressWarnings("unused")
public abstract class GCMsgBase<HdrType extends IGCSerializableHeader> extends AbstractMsgBase implements IClientGCMsg {

    private static final Logger logger = LogManager.getLogger(MsgBase.class);

    private HdrType header;

    /**
     * Initializes a new instance of the {@link GCMsgBase} class.
     *
     * @param clazz the type of the header
     */
    public GCMsgBase(Class<HdrType> clazz) {
        this(clazz, 0);
    }

    /**
     * Initializes a new instance of the {@link GCMsgBase} class.
     *
     * @param clazz          the type of the header
     * @param payloadReserve The number of bytes to initialize the payload capacity to.
     */
    public GCMsgBase(Class<HdrType> clazz, int payloadReserve) {
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
