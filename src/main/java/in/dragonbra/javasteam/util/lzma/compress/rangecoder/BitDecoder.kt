package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class BitDecoder {
    companion object {
        const val kNumBitModelTotalBits = 11
        const val kBitModelTotal = 1 shl kNumBitModelTotalBits
        private const val kNumMoveBits = 5
    }

    var prob: Int = 0

    fun updateModel(numMoveBits: Int, symbol: Int) {
        if (symbol == 0)
            prob += (kBitModelTotal - prob) shr numMoveBits
        else
            prob -= prob shr numMoveBits
    }

    fun init() {
        prob = kBitModelTotal shr 1
    }

    fun decode(rangeDecoder: Decoder): Int {
        val newBound = (rangeDecoder.range shr kNumBitModelTotalBits) * prob
        if (rangeDecoder.code < newBound) {
            rangeDecoder.range = newBound
            prob += (kBitModelTotal - prob) shr kNumMoveBits
            if (rangeDecoder.range < Decoder.kTopValue) {
                rangeDecoder.code = (rangeDecoder.code shl 8) or rangeDecoder.stream.read().toByte().toInt()
                rangeDecoder.range = rangeDecoder.range shl 8
            }
            return 0
        } else {
            rangeDecoder.range -= newBound
            rangeDecoder.code -= newBound
            prob -= prob shr kNumMoveBits
            if (rangeDecoder.range < Decoder.kTopValue) {
                rangeDecoder.code = (rangeDecoder.code shl 8) or rangeDecoder.stream.read().toByte().toInt()
                rangeDecoder.range = rangeDecoder.range shl 8
            }
            return 1
        }
    }
}
