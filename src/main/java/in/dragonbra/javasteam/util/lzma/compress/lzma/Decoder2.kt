package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitDecoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Decoder

class Decoder2 {
    private lateinit var mDecoders: Array<BitDecoder>

    fun create() {
        mDecoders = Array(0x300) { BitDecoder() }
    }

    fun init() {
        for (i in 0 until 0x300) {
            mDecoders[i].init()
        }
    }

    fun decodeNormal(rangeDecoder: Decoder): Byte {
        var symbol = 1
        do {
            symbol = (symbol shl 1) or mDecoders[symbol].decode(rangeDecoder)
        } while (symbol < 0x100)
        return symbol.toByte()
    }

    fun decodeWithMatchByte(rangeDecoder: Decoder, matchByte: Byte): Byte {
        var symbol = 1
        var matchByteLocal = matchByte.toInt()
        do {
            val matchBit = (matchByteLocal shr 7) and 1
            matchByteLocal = matchByteLocal shl 1
            val bit = mDecoders[((1 + matchBit) shl 8) + symbol].decode(rangeDecoder)
            symbol = (symbol shl 1) or bit
            if (matchBit != bit) {
                while (symbol < 0x100) {
                    symbol = (symbol shl 1) or mDecoders[symbol].decode(rangeDecoder)
                }
                break
            }
        } while (symbol < 0x100)
        return symbol.toByte()
    }
}
