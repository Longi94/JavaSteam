package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class BitTreeDecoder(private val numBitLevels: Int) {
    private val models: Array<BitDecoder> = Array(1 shl numBitLevels) { BitDecoder() }

    fun init() {
        for (i in 1 until (1 shl numBitLevels)) {
            models[i].init()
        }
    }

    fun decode(rangeDecoder: Decoder): Int {
        var m = 1
        for (bitIndex in numBitLevels downTo 1) {
            m = (m shl 1) + models[m].decode(rangeDecoder)
        }
        return m - (1 shl numBitLevels)
    }

    fun reverseDecode(rangeDecoder: Decoder): Int {
        var m = 1
        var symbol = 0
        for (bitIndex in 0 until numBitLevels) {
            val bit = models[m].decode(rangeDecoder)
            m = (m shl 1) + bit
            symbol = symbol or (bit shl bitIndex)
        }
        return symbol
    }

    companion object {
        fun reverseDecode(models: Array<BitDecoder>, startIndex: Int,
                          rangeDecoder: Decoder, numBitLevels: Int): Int {
            var m = 1
            var symbol = 0
            for (bitIndex in 0 until numBitLevels) {
                val bit = models[startIndex + m].decode(rangeDecoder)
                m = (m shl 1) + bit
                symbol = symbol or (bit shl bitIndex)
            }
            return symbol
        }
    }
}
