package `in`.dragonbra.javasteam.util.lzma.compress.lz

open class InWindow {
    var bufferBase: ByteArray? = null // pointer to buffer with data
    private var stream: java.io.InputStream? = null
    private var posLimit: Int = 0 // offset (from buffer) of first byte when new block reading must be done
    private var streamEndWasReached: Boolean = false // if (true) then streamPos shows real end of stream

    private var pointerToLastSafePosition: Int = 0

    var bufferOffset: Int = 0

    var blockSize: Int = 0 // Size of Allocated memory block
    var pos: Int = 0 // offset (from buffer) of current byte
    private var keepSizeBefore: Int = 0 // how many bytes must be kept in buffer before pos
    private var keepSizeAfter: Int = 0 // how many bytes must be kept buffer after pos
    var streamPos: Int = 0 // offset (from buffer) of first not read byte from Stream

    fun moveBlock() {
        var offset = bufferOffset + pos - keepSizeBefore
        // we need one additional byte, since movePos moves on 1 byte.
        if (offset > 0)
            offset--

        val numBytes = bufferOffset + streamPos - offset

        // check negative offset ????
        for (i in 0 until numBytes) {
            bufferBase!![i] = bufferBase!![offset + i]
        }
        bufferOffset -= offset
    }

    open fun readBlock() {
        if (streamEndWasReached)
            return
        while (true) {
            val size = (0 - bufferOffset) + blockSize - streamPos
            if (size == 0)
                return
            val numReadBytes = stream!!.read(bufferBase!!, bufferOffset + streamPos, size)
            if (numReadBytes == 0) {
                posLimit = streamPos
                val pointerToPosition = bufferOffset + posLimit
                if (pointerToPosition > pointerToLastSafePosition)
                    posLimit = pointerToLastSafePosition - bufferOffset

                streamEndWasReached = true
                return
            }
            streamPos += numReadBytes
            if (streamPos >= pos + keepSizeAfter)
                posLimit = streamPos - keepSizeAfter
        }
    }

    private fun free() {
        bufferBase = null
    }

    fun create(keepSizeBefore: Int, keepSizeAfter: Int, keepSizeReserv: Int) {
        this.keepSizeBefore = keepSizeBefore
        this.keepSizeAfter = keepSizeAfter
        val blockSize = keepSizeBefore + keepSizeAfter + keepSizeReserv
        if (bufferBase == null || this.blockSize != blockSize) {
            free()
            this.blockSize = blockSize
            bufferBase = ByteArray(this.blockSize)
        }
        pointerToLastSafePosition = this.blockSize - keepSizeAfter
    }

    open fun setStream(stream: java.io.InputStream) {
        this.stream = stream
    }

    open fun releaseStream() {
        stream = null
    }

    open fun init() {
        bufferOffset = 0
        pos = 0
        streamPos = 0
        streamEndWasReached = false
        readBlock()
    }

    open fun movePos() {
        pos++
        if (pos > posLimit) {
            val pointerToPosition = bufferOffset + pos
            if (pointerToPosition > pointerToLastSafePosition)
                moveBlock()
            readBlock()
        }
    }

    open fun getIndexByte(index: Int): Byte = bufferBase!![bufferOffset + pos + index]

    // index + limit have not to exceed keepSizeAfter
    open fun getMatchLen(index: Int, distance: Int, limit: Int): Int {
        var actualLimit = limit
        if (streamEndWasReached)
            if ((pos + index) + actualLimit > streamPos)
                actualLimit = streamPos - (pos + index)
        val actualDistance = distance + 1
        val pby = bufferOffset + pos + index

        var i = 0
        while (i < actualLimit && bufferBase!![pby + i] == bufferBase!![pby + i - actualDistance]) {
            i++
        }
        return i
    }

    open fun getNumAvailableBytes(): Int = streamPos - pos

    fun reduceOffsets(subValue: Int) {
        bufferOffset += subValue
        posLimit -= subValue
        pos -= subValue
        streamPos -= subValue
    }
}
