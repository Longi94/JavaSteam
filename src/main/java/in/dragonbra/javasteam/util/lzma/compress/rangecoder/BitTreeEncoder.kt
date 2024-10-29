package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class BitTreeEncoder(private val numBitLevels: Int) {
    private val models: Array<BitEncoder> = Array(1 shl numBitLevels) { BitEncoder() }

    fun init() {
        for (i in 1 until (1 shl numBitLevels)) {
            models[i].init()
        }
    }

    fun encode(rangeEncoder: Encoder, symbol: Int) {
        var m = 1
        for (bitIndex in numBitLevels - 1 downTo 0) {
            val bit = (symbol shr bitIndex) and 1
            models[m].encode(rangeEncoder, bit)
            m = (m shl 1) or bit
        }
    }

    fun reverseEncode(rangeEncoder: Encoder, symbol: Int) {
        var m = 1
        var s = symbol
        for (i in 0 until numBitLevels) {
            val bit = s and 1
            models[m].encode(rangeEncoder, bit)
            m = (m shl 1) or bit
            s = s shr 1
        }
    }

    fun getPrice(symbol: Int): Int {
        var price = 0
        var m = 1
        for (bitIndex in numBitLevels - 1 downTo 0) {
            val bit = (symbol shr bitIndex) and 1
            price += models[m].getPrice(bit)
            m = (m shl 1) + bit
        }
        return price
    }

    fun reverseGetPrice(symbol: Int): Int {
        var price = 0
        var m = 1
        var s = symbol
        for (i in numBitLevels downTo 1) {
            val bit = s and 1
            s = s shr 1
            price += models[m].getPrice(bit)
            m = (m shl 1) or bit
        }
        return price
    }

    companion object {
        fun reverseGetPrice(models: Array<BitEncoder>, startIndex: Int,
                            numBitLevels: Int, symbol: Int): Int {
            var price = 0
            var m = 1
            var s = symbol
            for (i in numBitLevels downTo 1) {
                val bit = s and 1
                s = s shr 1
                price += models[startIndex + m].getPrice(bit)
                m = (m shl 1) or bit
            }
            return price
        }

        fun reverseEncode(models: Array<BitEncoder>, startIndex: Int,
                          rangeEncoder: Encoder, numBitLevels: Int, symbol: Int) {
            var m = 1
            var s = symbol
            for (i in 0 until numBitLevels) {
                val bit = s and 1
                models[startIndex + m].encode(rangeEncoder, bit)
                m = (m shl 1) or bit
                s = s shr 1
            }
        }
    }
}
