package `in`.dragonbra.javasteam.networking.steam3

import `in`.dragonbra.javasteam.util.event.EventArgs
import java.net.InetSocketAddress

/**
 * @author lngtr
 * @since 2018-02-20
 *
 * Represents data that has been received over the network.
 */
@Suppress("unused")
class NetMsgEventArgs(val data: ByteArray, val endPoint: InetSocketAddress?) : EventArgs() {
    fun withData(data: ByteArray): NetMsgEventArgs = NetMsgEventArgs(data, endPoint)
}
