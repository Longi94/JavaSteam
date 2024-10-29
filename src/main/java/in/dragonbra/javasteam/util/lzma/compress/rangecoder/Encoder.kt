package `in`.dragonbra.javasteam.util.lzma.compress.rangecoder

class Encoder {
    companion object {
        const val kTopValue: Int = 1 shl 24
    }

    private lateinit var stream: java.io.OutputStream

    var low: Long = 0
    var range: Int = 0
    private var cacheSize: Int = 0
    private var cache: Byte = 0

    private var startPosition: Long = 0

    fun setStream(stream: java.io.OutputStream) {
        this.stream = stream
    }

    fun releaseStream() {
        // In Kotlin, we don't need to explicitly set to null
    }

    fun init() {
        startPosition = 0 // Assuming we start at the beginning of the stream

        low = 0
        range = 0xFFFFFFFF.toInt()
        cacheSize = 1
        cache = 0
    }

    fun flushData() {
        repeat(5) {
            shiftLow()
        }
    }

    fun flushStream() {
        stream.flush()
    }

    fun closeStream() {
        stream.close()
    }

    fun encode(start: Int, size: Int, total: Int) {
        low += (start.toLong() * (range / total))
        range *= size
        while (range < kTopValue) {
            range = range shl 8
            shiftLow()
        }
    }

    fun shiftLow() {
        if (low < 0xFF000000L || (low shr 32) == 1L) {
            var temp = cache
            do {
                stream.write((temp + (low shr 32)).toByte().toInt())
                temp = 0xFF.toByte()
            } while (--cacheSize != 0)
            cache = (low shr 24).toByte()
        }
        cacheSize++
        low = (low and 0xFFFFFF) shl 8
    }

    fun encodeDirectBits(v: Int, numTotalBits: Int) {
        for (i in numTotalBits - 1 downTo 0) {
            range = range shr 1
            if (((v shr i) and 1) == 1)
                low += range.toLong()
            if (range < kTopValue) {
                range = range shl 8
                shiftLow()
            }
        }
    }

    fun encodeBit(size0: Int, numTotalBits: Int, symbol: Int) {
        val newBound = (range shr numTotalBits) * size0
        if (symbol == 0)
            range = newBound
        else {
            low += newBound.toLong()
            range -= newBound
        }
        while (range < kTopValue) {
            range = range shl 8
            shiftLow()
        }
    }

    fun getProcessedSizeAdd(): Long {
        return cacheSize.toLong() + 4
    }
}
