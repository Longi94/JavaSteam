package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitEncoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitTreeEncoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Encoder

open class LenEncoder {
    private val choice = BitEncoder()
    private val choice2 = BitEncoder()
    private val lowCoder = Array(Base.kNumPosStatesEncodingMax) { BitTreeEncoder(Base.kNumLowLenBits) }
    private val midCoder = Array(Base.kNumPosStatesEncodingMax) { BitTreeEncoder(Base.kNumMidLenBits) }
    private val highCoder = BitTreeEncoder(Base.kNumHighLenBits)

    fun init(numPosStates: Int) {
        choice.init()
        choice2.init()
        for (posState in 0 until numPosStates) {
            lowCoder[posState].init()
            midCoder[posState].init()
        }
        highCoder.init()
    }

    open fun encode(rangeEncoder: Encoder, symbol: Int, posState: Int) {
        if (symbol < Base.kNumLowLenSymbols) {
            choice.encode(rangeEncoder, 0)
            lowCoder[posState].encode(rangeEncoder, symbol)
        } else {
            val adjustedSymbol = symbol - Base.kNumLowLenSymbols
            choice.encode(rangeEncoder, 1)
            if (adjustedSymbol < Base.kNumMidLenSymbols) {
                choice2.encode(rangeEncoder, 0)
                midCoder[posState].encode(rangeEncoder, adjustedSymbol)
            } else {
                choice2.encode(rangeEncoder, 1)
                highCoder.encode(rangeEncoder, adjustedSymbol - Base.kNumMidLenSymbols)
            }
        }
    }

    fun setPrices(posState: Int, numSymbols: Int, prices: IntArray, st: Int) {
        val a0 = choice.getPrice0()
        val a1 = choice.getPrice1()
        val b0 = a1 + choice2.getPrice0()
        val b1 = a1 + choice2.getPrice1()
        var i = 0
        while (i < Base.kNumLowLenSymbols) {
            if (i >= numSymbols)
                return
            prices[st + i] = a0 + lowCoder[posState].getPrice(i)
            i++
        }
        while (i < Base.kNumLowLenSymbols + Base.kNumMidLenSymbols) {
            if (i >= numSymbols)
                return
            prices[st + i] = b0 + midCoder[posState].getPrice(i - Base.kNumLowLenSymbols)
            i++
        }
        while (i < numSymbols) {
            prices[st + i] = b1 + highCoder.getPrice(i - Base.kNumLowLenSymbols - Base.kNumMidLenSymbols)
            i++
        }
    }
}
