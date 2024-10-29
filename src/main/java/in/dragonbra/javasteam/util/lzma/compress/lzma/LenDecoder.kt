package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Decoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitDecoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitTreeDecoder

class LenDecoder {
    private val mChoice = BitDecoder()
    private val mChoice2 = BitDecoder()
    private val mLowCoder = Array(Base.kNumPosStatesMax) { BitTreeDecoder(Base.kNumLowLenBits) }
    private val mMidCoder = Array(Base.kNumPosStatesMax) { BitTreeDecoder(Base.kNumMidLenBits) }
    private val mHighCoder = BitTreeDecoder(Base.kNumHighLenBits)
    private var mNumPosStates: Int = 0

    fun create(numPosStates: Int) {
        for (posState in mNumPosStates until numPosStates) {
            mLowCoder[posState] = BitTreeDecoder(Base.kNumLowLenBits)
            mMidCoder[posState] = BitTreeDecoder(Base.kNumMidLenBits)
        }
        mNumPosStates = numPosStates
    }

    fun init() {
        mChoice.init()
        for (posState in 0 until mNumPosStates) {
            mLowCoder[posState].init()
            mMidCoder[posState].init()
        }
        mChoice2.init()
        mHighCoder.init()
    }

    fun decode(rangeDecoder: Decoder, posState: Int): Int {
        return if (mChoice.decode(rangeDecoder) == 0) {
            mLowCoder[posState].decode(rangeDecoder)
        } else {
            var symbol = Base.kNumLowLenSymbols
            if (mChoice2.decode(rangeDecoder) == 0) {
                symbol += mMidCoder[posState].decode(rangeDecoder)
            } else {
                symbol += Base.kNumMidLenSymbols
                symbol += mHighCoder.decode(rangeDecoder)
            }
            symbol
        }
    }
}
