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

    /**
     * Strips off the protobuf message flag and returns an EMsg.
     *
     * @param msg The message number.
     * @return The underlying EMsg.
     */
    public static int getGCMsg(int msg) {
        return msg & EMSG_MASK;
    }

    /**
     * Crafts an EMsg, flagging it if required.
     *
     * @param msg      The EMsg to flag.
     * @param protobuf if set to true, the message is protobuf flagged.
     * @return A crafted EMsg, flagged if requested.
     */
    public static int makeMsg(int msg, boolean protobuf) {
        if (protobuf) {
            return msg | PROTO_MASK;
        }

        return msg;
    }

    /**
     * Crafts an EMsg, flagging it if required.
     *
     * @param msg      The EMsg to flag.
     * @param protobuf if set to <b>true</b>, the message is protobuf flagged.
     * @return A crafted EMsg, flagged if requested
     */
    public static int makeGCMsg(int msg, boolean protobuf) {
        if (protobuf) {
            return msg | PROTO_MASK;
        }
        return msg;
    }

    /**
     * Determines whether message is protobuf flagged.
     *
     * @param msg The message.
     * @return <b>true</b> if this message is protobuf flagged; otherwise, <b>false</b>.
     */
    public static boolean isProtoBuf(int msg) {
        return (msg & 0xffffffffL & PROTO_MASK) > 0;
    }
}
