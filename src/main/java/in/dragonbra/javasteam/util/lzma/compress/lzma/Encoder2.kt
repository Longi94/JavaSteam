package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitEncoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Encoder

class Encoder2 {
    private lateinit var mEncoders: Array<BitEncoder>

    fun create() {
        mEncoders = Array(0x300) { BitEncoder() }
    }

    fun init() {
        for (i in 0 until 0x300) {
            mEncoders[i].init()
        }
    }

    fun encode(rangeEncoder: Encoder, symbol: Byte) {
        var context = 1
        for (i in 7 downTo 0) {
            val bit = (symbol.toInt() shr i) and 1
            mEncoders[context].encode(rangeEncoder, bit)
            context = (context shl 1) or bit
        }
    }

    fun encodeMatched(rangeEncoder: Encoder, matchByte: Byte, symbol: Byte) {
        var context = 1
        var same = true
        for (i in 7 downTo 0) {
            val bit = (symbol.toInt() shr i) and 1
            var state = context
            if (same) {
                val matchBit = (matchByte.toInt() shr i) and 1
                state += ((1 + matchBit) shl 8)
                same = (matchBit == bit)
            }
            mEncoders[state].encode(rangeEncoder, bit)
            context = (context shl 1) or bit
        }
    }

    fun getPrice(matchMode: Boolean, matchByte: Byte, symbol: Byte): Int {
        var price = 0
        var context = 1
        var i = 7
        if (matchMode) {
            while (i >= 0) {
                val matchBit = (matchByte.toInt() shr i) and 1
                val bit = (symbol.toInt() shr i) and 1
                price += mEncoders[((1 + matchBit) shl 8) + context].getPrice(bit)
                context = (context shl 1) or bit
                if (matchBit != bit) {
                    i--
                    break
                }
                i--
            }
        }
        while (i >= 0) {
            val bit = (symbol.toInt() shr i) and 1
            price += mEncoders[context].getPrice(bit)
            context = (context shl 1) or bit
            i--
        }
        return price
    }
}
