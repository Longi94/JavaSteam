package `in`.dragonbra.javasteam.util

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.generated.MsgClientLogon
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgIPAddress
import org.apache.commons.validator.routines.InetAddressValidator
import java.lang.IllegalArgumentException
import java.net.DatagramSocket
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer

/**
 * @author lngtr
 * @since 2018-02-22
 */
object NetHelpers {

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun getIPAddress(ipAddr: InetAddress): Int {
        require(ipAddr.address.size == 4) { "only works with IPv4 addresses." }

        return ByteBuffer.wrap(ipAddr.address).int // and 0xFFFFFFFFL.toInt()
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getLocalIP(socket: Socket): InetAddress? = getLocalIP(socket.localAddress)

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun getLocalIP(datagramSocket: DatagramSocket): InetAddress? = getLocalIP(datagramSocket.localAddress)

    @JvmStatic
    fun getLocalIP(endpoint: InetAddress?): InetAddress? {
        if (endpoint == null || endpoint.address == InetAddress.getByName("0.0.0.0")) {
            return null
        }

        return endpoint
    }

    @JvmStatic
    fun getIPAddress(ipAddr: Int): InetAddress {
        val result = byteArrayOf(
            ((ipAddr shr 24) and 0xFF).toByte(),
            ((ipAddr shr 16) and 0xFF).toByte(),
            ((ipAddr shr 8) and 0xFF).toByte(),
            (ipAddr and 0xFF).toByte()
        )
        return InetAddress.getByAddress(result)
    }

    @JvmStatic
    fun getIPAddress(ipAddr: CMsgIPAddress): InetAddress {
        return if (ipAddr.hasV6()) {
            InetAddress.getByAddress(ipAddr.v6.toByteArray())
        } else {
            getIPAddress(ipAddr.v4)
        }
    }

    @JvmStatic
    fun getMsgIPAddress(ipAddr: InetAddress): CMsgIPAddress {
        val msgIpAddress = CMsgIPAddress.newBuilder()

        if (ipAddr is Inet6Address) {
            msgIpAddress.v6 = ByteString.copyFrom(ipAddr.address)
        } else {
            msgIpAddress.v4 = getIPAddress(ipAddr)
        }

        return msgIpAddress.build()
    }

    @JvmStatic
    fun obfuscatePrivateIP(msgIpAddress: CMsgIPAddress): CMsgIPAddress {
        val localIp = msgIpAddress.toBuilder()

        if (localIp.hasV6()) {
            // TODO v6 validation
            val v6Bytes = msgIpAddress.v6.toByteArray()
            for (i in 0..15 step 4) {
                v6Bytes[i] = (v6Bytes[i].toInt() xor 0x0D).toByte()
                v6Bytes[i + 1] = (v6Bytes[i + 1].toInt() xor 0xF0).toByte()
                v6Bytes[i + 2] = (v6Bytes[i + 2].toInt() xor 0xAD).toByte()
                v6Bytes[i + 3] = (v6Bytes[i + 3].toInt() xor 0xBA).toByte()
            }
            localIp.v6 = ByteString.copyFrom(v6Bytes)
        } else {
            localIp.v4 = msgIpAddress.v4 xor MsgClientLogon.ObfuscationMask
        }

        return localIp.build()
    }

    @JvmStatic
    fun tryParseIPEndPoint(stringValue: String): InetSocketAddress? {
        try {
            val split = stringValue.lastIndexOf(':')
            if (split == -1) {
                return null
            }

            var ip = stringValue.substring(0, split)
            val port = stringValue.substring(split + 1).toInt()

            if (ip.startsWith("[") && ip.endsWith("]")) {
                ip = ip.substring(1, ip.length - 1) // Remove the brackets
            }

            val validator = InetAddressValidator.getInstance()
            return if (validator.isValidInet4Address(ip) || validator.isValidInet6Address(ip)) {
                InetSocketAddress(ip, port)
            } else {
                null
            }
        } catch (_: Exception) {
            return null
        }
    }
}
