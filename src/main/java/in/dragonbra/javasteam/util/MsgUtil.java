package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EMsg;

/**
 * @author lngtr
 * @since 2018-02-21
 */
public class MsgUtil {

    private static final int PROTO_MASK = 0x80000000;
    private static final int EMSG_MASK = ~PROTO_MASK;

    /**
     * Strips off the protobuf message flag and returns an EMsg.
     *
     * @param msg The message number.
     * @return The underlying EMsg.
     */
    public static EMsg getMsg(int msg) {
        return EMsg.from(msg & EMSG_MASK);
    }
}
