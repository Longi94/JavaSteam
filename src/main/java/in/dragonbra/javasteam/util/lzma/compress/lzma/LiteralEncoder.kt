package `in`.dragonbra.javasteam.util.lzma.compress.lzma

class LiteralEncoder {
    private lateinit var mCoders: Array<Encoder2>
    private var mNumPrevBits: Int = 0
    private var mNumPosBits: Int = 0
    private var mPosMask: Int = 0

    fun create(numPosBits: Int, numPrevBits: Int) {
        if (::mCoders.isInitialized && mNumPrevBits == numPrevBits && mNumPosBits == numPosBits)
            return
        mNumPosBits = numPosBits
        mPosMask = (1 shl numPosBits) - 1
        mNumPrevBits = numPrevBits
        val numStates = 1 shl (mNumPrevBits + mNumPosBits)
        mCoders = Array(numStates) { Encoder2() }
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

    fun getSubCoder(pos: Int, prevByte: Byte): Encoder2 {
        return mCoders[((pos and mPosMask) shl mNumPrevBits) + (prevByte.toInt() ushr (8 - mNumPrevBits))]
    }
}
