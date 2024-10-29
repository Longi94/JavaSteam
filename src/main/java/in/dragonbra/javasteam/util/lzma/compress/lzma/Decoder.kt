package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.DataErrorException
import `in`.dragonbra.javasteam.util.lzma.ICodeProgress
import `in`.dragonbra.javasteam.util.lzma.ICoder
import `in`.dragonbra.javasteam.util.lzma.ISetDecoderProperties
import `in`.dragonbra.javasteam.util.lzma.compress.lz.OutWindow
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitDecoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitTreeDecoder
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException

class Decoder : ICoder, ISetDecoderProperties {
    private val mOutWindow = OutWindow()
    private val mRangeDecoder = `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Decoder()

    private val mIsMatchDecoders = Array(Base.kNumStates shl Base.kNumPosStatesBitsMax) { BitDecoder() }
    private val mIsRepDecoders = Array(Base.kNumStates) { BitDecoder() }
    private val mIsRepG0Decoders = Array(Base.kNumStates) { BitDecoder() }
    private val mIsRepG1Decoders = Array(Base.kNumStates) { BitDecoder() }
    private val mIsRepG2Decoders = Array(Base.kNumStates) { BitDecoder() }
    private val mIsRep0LongDecoders = Array(Base.kNumStates shl Base.kNumPosStatesBitsMax) { BitDecoder() }

    private val mPosSlotDecoder = Array(Base.kNumLenToPosStates) { BitTreeDecoder(Base.kNumPosSlotBits) }
    private val mPosDecoders = Array(Base.kNumFullDistances - Base.kEndPosModelIndex) { BitDecoder() }

    private val mPosAlignDecoder = BitTreeDecoder(Base.kNumAlignBits)

    private val mLenDecoder = LenDecoder()
    private val mRepLenDecoder = LenDecoder()

    private val mLiteralDecoder = LiteralDecoder()

    private var mDictionarySize: Int = 0
    private var mDictionarySizeCheck: Int = 0

    private var mPosStateMask: Int = 0

    private var mAllowIllegalStreamStart: Boolean = false // Added by SteamKit

    constructor() {
        mDictionarySize = Int.MAX_VALUE
    }

    constructor(allowIllegalStreamStart: Boolean) : this() {
        mAllowIllegalStreamStart = allowIllegalStreamStart
    }

    private fun setDictionarySize(dictionarySize: Int) {
        if (mDictionarySize != dictionarySize) {
            mDictionarySize = dictionarySize
            mDictionarySizeCheck = maxOf(mDictionarySize, 1)
            val blockSize = maxOf(mDictionarySizeCheck, 1 shl 12)
            mOutWindow.create(blockSize)
        }
    }

    private fun setLiteralProperties(lp: Int, lc: Int) {
        if (lp > 8 || lc > 8)
            throw InvalidParameterException()
        mLiteralDecoder.create(lp, lc)
    }

    private fun setPosBitsProperties(pb: Int) {
        if (pb > Base.kNumPosStatesBitsMax)
            throw InvalidParameterException()
        val numPosStates = 1 shl pb
        mLenDecoder.create(numPosStates)
        mRepLenDecoder.create(numPosStates)
        mPosStateMask = numPosStates - 1
    }

    private var solid = false
    private fun init(inStream: InputStream, outStream: OutputStream) {
        mRangeDecoder.init(inStream)
        mOutWindow.init(outStream, solid)

        for (i in 0 until Base.kNumStates) {
            for (j in 0..mPosStateMask) {
                val index = (i shl Base.kNumPosStatesBitsMax) + j
                mIsMatchDecoders[index].init()
                mIsRep0LongDecoders[index].init()
            }
            mIsRepDecoders[i].init()
            mIsRepG0Decoders[i].init()
            mIsRepG1Decoders[i].init()
            mIsRepG2Decoders[i].init()
        }

        mLiteralDecoder.init()
        for (i in 0 until Base.kNumLenToPosStates) {
            mPosSlotDecoder[i].init()
        }
        for (i in 0 until Base.kNumFullDistances - Base.kEndPosModelIndex) {
            mPosDecoders[i].init()
        }

        mLenDecoder.init()
        mRepLenDecoder.init()
        mPosAlignDecoder.init()
    }

    override fun code(inStream: InputStream, outStream: OutputStream, inSize: Long, outSize: Long, progress: ICodeProgress?) {
        init(inStream, outStream)

        val state = State()
        state.init()
        var rep0 = 0
        var rep1 = 0
        var rep2 = 0
        var rep3 = 0

        var nowPos64: Long = 0
        val outSize64 = outSize

        if (nowPos64 < outSize64 && !mAllowIllegalStreamStart) {
            if (mIsMatchDecoders[state.index shl Base.kNumPosStatesBitsMax].decode(mRangeDecoder) != 0)
                throw DataErrorException()
            state.updateChar()
            val b = mLiteralDecoder.decodeNormal(mRangeDecoder, 0, 0)
            mOutWindow.putByte(b)
            nowPos64++
        }
        while (nowPos64 < outSize64) {
            val posState = nowPos64.toInt() and mPosStateMask
            if (mIsMatchDecoders[(state.index shl Base.kNumPosStatesBitsMax) + posState].decode(mRangeDecoder) == 0) {
                val prevByte = mOutWindow.getByte(0)
                val b = if (!state.isCharState())
                    mLiteralDecoder.decodeWithMatchByte(mRangeDecoder, nowPos64.toInt(), prevByte, mOutWindow.getByte(rep0))
                else
                    mLiteralDecoder.decodeNormal(mRangeDecoder, nowPos64.toInt(), prevByte)
                mOutWindow.putByte(b)
                state.updateChar()
                nowPos64++
            } else {
                var len: Int
                if (mIsRepDecoders[state.index].decode(mRangeDecoder) == 1) {
                    if (mIsRepG0Decoders[state.index].decode(mRangeDecoder) == 0) {
                        if (mIsRep0LongDecoders[(state.index shl Base.kNumPosStatesBitsMax) + posState].decode(mRangeDecoder) == 0) {
                            state.updateShortRep()
                            mOutWindow.putByte(mOutWindow.getByte(rep0))
                            nowPos64++
                            continue
                        }
                    } else {
                        val distance: Int
                        if (mIsRepG1Decoders[state.index].decode(mRangeDecoder) == 0) {
                            distance = rep1
                        } else {
                            if (mIsRepG2Decoders[state.index].decode(mRangeDecoder) == 0)
                                distance = rep2
                            else {
                                distance = rep3
                                rep3 = rep2
                            }
                            rep2 = rep1
                        }
                        rep1 = rep0
                        rep0 = distance
                    }
                    len = mRepLenDecoder.decode(mRangeDecoder, posState) + Base.kMatchMinLen
                    state.updateRep()
                } else {
                    rep3 = rep2
                    rep2 = rep1
                    rep1 = rep0
                    len = Base.kMatchMinLen + mLenDecoder.decode(mRangeDecoder, posState)
                    state.updateMatch()
                    val posSlot = mPosSlotDecoder[Base.getLenToPosState(len)].decode(mRangeDecoder)
                    if (posSlot >= Base.kStartPosModelIndex) {
                        val numDirectBits = (posSlot shr 1) - 1
                        rep0 = (2 or (posSlot and 1)) shl numDirectBits
                        if (posSlot < Base.kEndPosModelIndex)
                            rep0 += BitTreeDecoder.reverseDecode(mPosDecoders,
                                rep0 - posSlot - 1, mRangeDecoder, numDirectBits)
                        else {
                            rep0 += mRangeDecoder.decodeDirectBits(
                                numDirectBits - Base.kNumAlignBits) shl Base.kNumAlignBits
                            rep0 += mPosAlignDecoder.reverseDecode(mRangeDecoder)
                        }
                    } else
                        rep0 = posSlot
                }
                if (rep0 >= mOutWindow.trainSize + nowPos64 || rep0 >= mDictionarySizeCheck) {
                    if (rep0 == 0xFFFFFFFF.toInt())
                        break
                    throw DataErrorException()
                }
                mOutWindow.copyBlock(rep0, len)
                nowPos64 += len
            }
        }
        mOutWindow.flush()
        mOutWindow.releaseStream()
        mRangeDecoder.releaseStream()
    }

    override fun setDecoderProperties(properties: ByteArray) {
        if (properties.size < 5)
            throw InvalidParameterException()
        val lc = properties[0] % 9
        val remainder = properties[0] / 9
        val lp = remainder % 5
        val pb = remainder / 5
        if (pb > Base.kNumPosStatesBitsMax)
            throw InvalidParameterException()
        var dictionarySize = 0
        for (i in 0 until 4)
            dictionarySize += (properties[1 + i].toInt() and 0xFF) shl (i * 8)
        setDictionarySize(dictionarySize)
        setLiteralProperties(lp, lc)
        setPosBitsProperties(pb)
    }

    fun steamKitSetDecoderProperties(bits: Byte, dictionarySize: Int, buffer: ByteArray) {
        val lc = bits % 9
        val remainder = bits / 9
        val lp = remainder % 5
        val pb = remainder / 5
        if (pb > Base.kNumPosStatesBitsMax || dictionarySize < (1 shl 12))
            throw InvalidParameterException()
        setLiteralProperties(lp, lc)
        setPosBitsProperties(pb)
        mDictionarySize = dictionarySize
        mDictionarySizeCheck = dictionarySize
        mOutWindow.steamKitSetBuffer(buffer, dictionarySize)
    }

    fun train(stream: InputStream): Boolean {
        solid = true
        return mOutWindow.train(stream)
    }
}
