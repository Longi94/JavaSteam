package `in`.dragonbra.javasteam.base.gc

import `in`.dragonbra.javasteam.base.ISteamSerializable

/**
 * @author lngtr
 * @since 2018-02-21
 */
interface IGCSerializableMessage : ISteamSerializable {
    val eMsg: Int
}
