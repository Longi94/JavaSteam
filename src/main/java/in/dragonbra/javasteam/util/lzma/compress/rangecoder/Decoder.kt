package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class Decoder {
    companion object {
        const val kTopValue: Int = 1 shl 24
    }

    var range: Int = 0
    var code: Int = 0
    lateinit var stream: java.io.InputStream

    fun init(stream: java.io.InputStream) {
        this.stream = stream

        code = 0
        range = 0xFFFFFFFF.toInt()
        for (i in 0 until 5) {
            code = (code shl 8) or (stream.read().toByte().toInt())
        }
    }

    fun releaseStream() {
        stream.close()
    }

    fun closeStream() {
        stream.close()
    }

    fun normalize() {
        while (range < kTopValue) {
            code = (code shl 8) or (stream.read().toByte().toInt())
            range = range shl 8
        }
    }

    fun normalize2() {
        if (range < kTopValue) {
            code = (code shl 8) or (stream.read().toByte().toInt())
            range = range shl 8
        }
    }

    fun getThreshold(total: Int): Int {
        range /= total
        return code / range
    }

    fun decode(start: Int, size: Int, total: Int) {
        code -= start * range
        range *= size
        normalize()
    }

    fun decodeDirectBits(numTotalBits: Int): Int {
        var range = this.range
        var code = this.code
        var result = 0
        for (i in numTotalBits downTo 1) {
            range = range shr 1
            val t = (code - range) shr 31
            code -= range and (t - 1)
            result = (result shl 1) or (1 - t)

            if (range < kTopValue) {
                code = (code shl 8) or (stream.read().toByte().toInt())
                range = range shl 8
            }
        }
        this.range = range
        this.code = code
        return result
    }

    fun decodeBit(size0: Int, numTotalBits: Int): Int {
        val newBound = (range shr numTotalBits) * size0
        val symbol: Int
        if (code < newBound) {
            symbol = 0
            range = newBound
        } else {
            symbol = 1
            code -= newBound
            range -= newBound
        }
        normalize()
        return symbol
    }
}
