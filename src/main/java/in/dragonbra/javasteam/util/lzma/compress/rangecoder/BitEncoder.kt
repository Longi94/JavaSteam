package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class BitEncoder {
    companion object {
        const val kNumBitModelTotalBits = 11
        const val kBitModelTotal = 1 shl kNumBitModelTotalBits
        private const val kNumMoveBits = 5
        private const val kNumMoveReducingBits = 2
        const val kNumBitPriceShiftBits = 6

        private val probPrices = IntArray(kBitModelTotal shr kNumMoveReducingBits)

        init {
            val kNumBits = kNumBitModelTotalBits - kNumMoveReducingBits
            for (i in kNumBits - 1 downTo 0) {
                val start = 1 shl (kNumBits - i - 1)
                val end = 1 shl (kNumBits - i)
                for (j in start until end) {
                    probPrices[j] = (i shl kNumBitPriceShiftBits) +
                            (((end - j) shl kNumBitPriceShiftBits) shr (kNumBits - i - 1))
                }
            }
        }
    }

    var prob: Int = 0

    fun init() {
        prob = kBitModelTotal shr 1
    }

    fun updateModel(symbol: Int) {
        if (symbol == 0)
            prob += (kBitModelTotal - prob) shr kNumMoveBits
        else
            prob -= prob shr kNumMoveBits
    }

    fun encode(encoder: Encoder, symbol: Int) {
        val newBound = (encoder.range shr kNumBitModelTotalBits) * prob
        if (symbol == 0) {
            encoder.range = newBound
            prob += (kBitModelTotal - prob) shr kNumMoveBits
        } else {
            encoder.low += newBound.toLong()
            encoder.range -= newBound
            prob -= prob shr kNumMoveBits
        }
        if (encoder.range < Encoder.kTopValue) {
            encoder.range = encoder.range shl 8
            encoder.shiftLow()
        }
    }

    fun getPrice(symbol: Int): Int {
        return probPrices[((prob - symbol) xor (-symbol)) and (kBitModelTotal - 1) shr kNumMoveReducingBits]
    }

    fun getPrice0(): Int = probPrices[prob shr kNumMoveReducingBits]

    fun getPrice1(): Int = probPrices[(kBitModelTotal - prob) shr kNumMoveReducingBits]
}
