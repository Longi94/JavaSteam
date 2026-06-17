package `in`.dragonbra.javasteam.util

import `in`.dragonbra.javasteam.enums.EMsg

/**
 * @author lngtr
 * @since 2018-02-21
 */
object MsgUtil {
    private const val PROTO_MASK = -0x80000000
    private const val EMSG_MASK = PROTO_MASK.inv()

    /**
     * Strips off the protobuf message flag and returns an EMsg.
     * @param msg The message number.
     * @return The underlying EMsg.
     */
    @JvmStatic
    fun getMsg(msg: Int): EMsg? = EMsg.from(msg and EMSG_MASK)

    /**
     * Strips off the protobuf message flag and returns an EMsg.
     * @param msg The message number.
     * @return The underlying EMsg.
     */
    @JvmStatic
    fun getGCMsg(msg: Int): Int = msg and EMSG_MASK

    /**
     * Crafts an EMsg, flagging it if required.
     * @param msg      The EMsg to flag.
     * @param protobuf if set to true, the message is protobuf flagged.
     * @return A crafted EMsg, flagged if requested.
     */
    @JvmStatic
    fun makeMsg(msg: Int, protobuf: Boolean): Int {
        if (protobuf) {
            return msg or PROTO_MASK
        }

        return msg
    }

    /**
     * Crafts an EMsg, flagging it if required.
     * @param msg      The EMsg to flag.
     * @param protobuf if set to **true**, the message is protobuf flagged.
     * @return A crafted EMsg, flagged if requested
     */
    @JvmStatic
    fun makeGCMsg(msg: Int, protobuf: Boolean): Int {
        if (protobuf) {
            return msg or PROTO_MASK
        }
        return msg
    }

    /**
     * Determines whether message is protobuf flagged.
     * @param msg The message.
     * @return **true** if this message is protobuf flagged; otherwise, **false**.
     */
    @JvmStatic
    fun isProtoBuf(msg: Int): Boolean = (msg.toLong() and 0xffffffffL and PROTO_MASK.toLong()) > 0
}
