package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Decoder

class LiteralDecoder {
    private lateinit var mCoders: Array<Decoder2>
    private var mNumPrevBits: Int = 0
    private var mNumPosBits: Int = 0
    private var mPosMask: Int = 0

    fun create(numPosBits: Int, numPrevBits: Int) {
        if (::mCoders.isInitialized && mNumPrevBits == numPrevBits &&
            mNumPosBits == numPosBits
        ) {
            return
        }
        mNumPosBits = numPosBits
        mPosMask = (1 shl numPosBits) - 1
        mNumPrevBits = numPrevBits
        val numStates = 1 shl (mNumPrevBits + mNumPosBits)
        mCoders = Array(numStates) { Decoder2() }
        for (i in 0 until numStates) {
            mCoders[i].create()
        }
    }

    fun init() {
        val numStates = 1 shl (mNumPrevBits + mNumPosBits)
        for (i in 0 until numStates) {
            mCoders[i].init()
        }
    }

    private fun getState(pos: Int, prevByte: Byte): Int {
        return ((pos and mPosMask) shl mNumPrevBits) + (prevByte.toInt() ushr (8 - mNumPrevBits))
    }

    fun decodeNormal(rangeDecoder: Decoder, pos: Int, prevByte: Byte): Byte {
        return mCoders[getState(pos, prevByte)].decodeNormal(rangeDecoder)
    }

    fun decodeWithMatchByte(rangeDecoder: Decoder, pos: Int, prevByte: Byte, matchByte: Byte): Byte {
        return mCoders[getState(pos, prevByte)].decodeWithMatchByte(rangeDecoder, matchByte)
    }
}
